package dialight.nblauncher.controller;


import dialight.minecraft.MinecraftAccount;
import dialight.minecraft.Yggdrassil;
import dialight.minecraft.json.AccountsState;
import dialight.misc.*;
import dialight.mvc.Controller;
import dialight.mvc.MVCApplication;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AccountsController extends Controller {

    private final File accountsFile = new File("accounts.json");

    private final ObservableList<MinecraftAccount> accounts = FXCollections.observableArrayList();

    private final SimpleObjectProperty<MinecraftAccount> selectedAccount = new SimpleObjectProperty<>(null);
    private final BooleanBinding hasAccount = Bindings.createBooleanBinding(() -> selectedAccount.getValue() != null, selectedAccount);

    private final SimpleObjectProperty<String> username = new SimpleObjectProperty<>(null);
    private final SimpleObjectProperty<String> password = new SimpleObjectProperty<>(null);

    private final Supplier<SimpleTask<Boolean>> saveAccounts = () -> new SimpleTask<Boolean>() {

        private List<MinecraftAccount> accounts;
        private UUID selected;

        @Override public void uiInit() {
            updateMessage("save accounts");
            accounts = new ArrayList<>(AccountsController.this.accounts);
            MinecraftAccount selectedProfile = AccountsController.this.selectedAccount.get();
            if(selectedProfile != null) {
                selected = selectedProfile.getUuid();
            }
        }

        @Override protected Boolean call() throws Exception {
            TextUtils.writeText(accountsFile, Json.build(new AccountsState(accounts, selected)).toString());
            return true;
        }

    };
    private final Supplier<SimpleTask<AccountsState>> loadAccounts = () -> new SimpleTask<AccountsState>() {

        @Override public void uiInit() {
            updateMessage("load accounts");
        }

        @Override protected AccountsState call() throws Exception {
            if(!accountsFile.exists()) return null;
            String content = TextUtils.readText(accountsFile);
            return Json.GSON.fromJson(Json.parse(content), AccountsState.class);
        }

        @Override public void uiDone(@Nullable AccountsState value) {
            if(value != null) {
                UUID selectedUuid = value.getSelected();
                MinecraftAccount selected = null;
                for (MinecraftAccount profile : value.getProfiles()) {
                    if(!updateAccount(profile)) accounts.add(profile);
                    if(profile.getUuid().equals(selectedUuid)) {
                        selected = profile;
                    }
                }
                if(selected != null && selectedAccount.get() == null) {
                    selectedAccount.set(selected);
                }
            }
        }
    };

    private final Supplier<SimpleTask<MinecraftAccount>> authenticate = () -> new SimpleTask<MinecraftAccount>() {

        private String username;
        private String password;

        @Override public void uiInit() {
            updateMessage("authenticate");
            username = AccountsController.this.username.get();
            password = AccountsController.this.password.get();
        }
        @Override protected MinecraftAccount call() throws Exception {
            return Yggdrassil.authenticate(username, password);
        }

        @Override public void uiDone(@Nullable MinecraftAccount value) {
            if(value != null) {
                if(!updateAccount(value)) accounts.add(value);
                progress.scheduleTask(saveAccounts.get());
                if(selectedAccount.get() == null) {
                    selectedAccount.set(value);
                }
                AccountsController.this.username.set("");
                AccountsController.this.password.set("");
            }
        }
    };
    private final Supplier<SimpleTask<MinecraftAccount>> refresh = () -> new SimpleTask<MinecraftAccount>() {

        private MinecraftAccount account;

        @Override public void uiInit() {
            updateMessage("refresh access token");
            account = selectedAccount.get();
        }

        @Override protected MinecraftAccount call() throws Exception {
            return Yggdrassil.refresh(account);
        }

        @Override public void uiDone(@Nullable MinecraftAccount value) {
            if(value != null) {
                if(!updateAccount(value)) {
                    throw new IllegalStateException("Account not found");
                } else {
                    progress.scheduleTask(saveAccounts.get());
                }
            }
        }

    };
    private final Supplier<SimpleTask<Boolean>> validate = () -> new SimpleTask<Boolean>() {

        private MinecraftAccount account;

        @Override public void uiInit() {
            updateMessage("validate");
            account = selectedAccount.get();
            Objects.requireNonNull(account);
        }

        @Override protected Boolean call() throws Exception {
            return Yggdrassil.validate(account);
        }
    };
    private final Supplier<SimpleTask<Boolean>> invalidate = () -> new SimpleTask<Boolean>() {

        private MinecraftAccount account;

        @Override public void uiInit() {
            updateMessage("invalidate");
            account = selectedAccount.get();
        }

        @Override protected Boolean call() throws Exception {
            return Yggdrassil.invalidate(account);
        }
    };
    private ProgressController progress;

    @Override protected void init(MVCApplication app) {
        progress = app.findController(ProgressController.class);
        progress.scheduleTask(loadAccounts.get());
    }

    public boolean updateAccount(MinecraftAccount value) {
        MinecraftAccount selected = selectedAccount.getValue();
        if(selected != null && selected.getUuid().equals(value.getUuid())) selectedAccount.setValue(value);
        ListIterator<MinecraftAccount> it = accounts.listIterator();
        while(it.hasNext()) {
            MinecraftAccount cur = it.next();
            if(cur.getUuid().equals(value.getUuid())) {
                it.set(value);
                return true;
            }
        }
        return false;
    }

    public ObservableList<MinecraftAccount> getAccounts() {
        return accounts;
    }

    public MinecraftAccount getSelectedAccount() {
        return selectedAccount.get();
    }

    public Boolean hasAccount() {
        return hasAccount.get();
    }

    public BooleanBinding hasAccountProperty() {
        return hasAccount;
    }

    public SimpleObjectProperty<MinecraftAccount> selectedAccountProperty() {
        return selectedAccount;
    }

    public Property<String> usernameProperty() {
        return username;
    }
    public Property<String> passwordProperty() {
        return password;
    }

    public SimpleTask<MinecraftAccount> authenticate() {
        SimpleTask<MinecraftAccount> task = authenticate.get();
        progress.scheduleTask(task);
        return task;
    }

    public void delete(MinecraftAccount account) {
        accounts.remove(account);
        progress.scheduleTask(invalidate.get());
        progress.scheduleTask(saveAccounts.get());
    }

    public void validateSelected(Consumer<Boolean> op) {
        SimpleTask<Boolean> task = validate.get();
        progress.scheduleTask(task);
        task.setOnSucceeded(event -> {
            op.accept(task.getValue());
        });
    }

    public void refreshSelected(Consumer<Boolean> op) {
        SimpleTask<MinecraftAccount> task = refresh.get();
        progress.scheduleTask(task);
        task.runningProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                op.accept(task.getValue() != null);
            }
        });
    }

    public void loadAccountsSync() {
        if(!accountsFile.exists()) return;
        String content = TextUtils.readText(accountsFile);
        AccountsState state = Json.GSON.fromJson(Json.parse(content), AccountsState.class);

        UUID selectedUuid = state.getSelected();
        MinecraftAccount selected = null;
        for (MinecraftAccount profile : state.getProfiles()) {
            if(!updateAccount(profile)) accounts.add(profile);
            if(profile.getUuid().equals(selectedUuid)) {
                selected = profile;
            }
        }
        if(selected != null && selectedAccount.get() == null) {
            selectedAccount.set(selected);
        }
    }

    public void saveAccountsSync() {
        UUID selected = null;
        MinecraftAccount selectedAccount = this.selectedAccount.get();
        if(selectedAccount != null) {
            selected = selectedAccount.getUuid();
        }
        TextUtils.writeText(accountsFile, Json.build(new AccountsState(accounts, selected)).toString());
    }

}
