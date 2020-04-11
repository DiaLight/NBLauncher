package dialight.nblauncher.controller;


import dialight.minecraft.MinecraftAccount;
import dialight.minecraft.Yggdrassil;
import dialight.nblauncher.json.AccountsState;
import dialight.misc.*;
import dialight.mvc.Controller;
import dialight.mvc.InitCtx;
import dialight.mvc.MVCApplication;
import dialight.nblauncher.NBLauncher;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class AccountsController extends Controller {

    private final ObservableList<MinecraftAccount> accounts = FXCollections.observableArrayList();

    private final SimpleObjectProperty<MinecraftAccount> selectedAccount = new SimpleObjectProperty<>(null);
    private final BooleanBinding hasAccount = Bindings.createBooleanBinding(() -> selectedAccount.getValue() != null, selectedAccount);

    private final SimpleObjectProperty<String> username = new SimpleObjectProperty<>(null);
    private final SimpleObjectProperty<String> password = new SimpleObjectProperty<>(null);
    private final NBLauncher nbl;

    private ProgressController progress;

    public AccountsController(NBLauncher nbl) {
        this.nbl = nbl;
    }

    @Override protected void init(InitCtx ctx, MVCApplication app, Runnable done) {
        progress = app.findController(ProgressController.class);
        loadAccounts(() -> {
            ctx.fireInit(app, done);
        });
    }

    public boolean updateAccount(MinecraftAccount value) {
        Objects.requireNonNull(value);
        MinecraftAccount selected = selectedAccount.getValue();
        if(selected != null) {
            if(selected.getUuid().equals(value.getUuid())) selectedAccount.setValue(value);
        } else {
            selectedAccount.setValue(value);
        }
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
        SimpleTask<MinecraftAccount> task = authenticate_get();
        progress.scheduleTask(task);
        return task;
    }

    public void delete(MinecraftAccount account) {
        accounts.remove(account);
        progress.scheduleTask(invalidate_get());
        progress.scheduleTask(saveAccounts_get());
    }

    public void validateSelected(Consumer<Boolean> op) {
        SimpleTask<Boolean> task = validate_get();
        task.setOnSucceeded(event -> {
            op.accept(task.getValue());
        });
        progress.scheduleTask(task);
    }

    public void refreshSelected(Consumer<Boolean> op) {
        SimpleTask<MinecraftAccount> task = refresh_get();
        task.setOnSucceeded(event -> {
            op.accept(task.getValue() != null);
        });
        progress.scheduleTask(task);
    }

    public void saveAccountsSync() {
        UUID selected = null;
        MinecraftAccount selectedAccount = this.selectedAccount.get();
        if(selectedAccount != null) {
            selected = selectedAccount.getUuid();
        }
        TextUtils.writeText(nbl.nblPaths.accountsFile, Json.build(new AccountsState(accounts, selected)).toString(), StandardCharsets.UTF_8);
    }

    public void loadAccounts(Runnable done) {
        SimpleTask<AccountsState> task = loadAccounts_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }
    private SimpleTask<AccountsState> loadAccounts_get() {
        return new SimpleTask<AccountsState>() {

            @Override public void uiInit() {
                updateMessage("load accounts");
            }

            @Override protected AccountsState call() throws Exception {
                if(!Files.exists(nbl.nblPaths.accountsFile)) return null;
                String content = TextUtils.readText(nbl.nblPaths.accountsFile, StandardCharsets.UTF_8);
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
    }

    private SimpleTask<MinecraftAccount> authenticate_get() {
        return new SimpleTask<MinecraftAccount>() {

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
                    progress.scheduleTask(saveAccounts_get());
                    if(selectedAccount.get() == null) {
                        selectedAccount.set(value);
                    }
                    AccountsController.this.username.set("");
                    AccountsController.this.password.set("");
                }
            }
        };
    }

    private SimpleTask<Boolean> invalidate_get() {
        return new SimpleTask<Boolean>() {

            private MinecraftAccount account;

            @Override public void uiInit() {
                updateMessage("invalidate");
                account = selectedAccount.get();
            }

            @Override protected Boolean call() throws Exception {
                return Yggdrassil.invalidate(account);
            }
        };
    }

    private SimpleTask<Boolean> saveAccounts_get() {
        return new SimpleTask<Boolean>() {

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
                TextUtils.writeText(nbl.nblPaths.accountsFile, Json.build(new AccountsState(accounts, selected)).toString(), StandardCharsets.UTF_8);
                return true;
            }

        };
    }

    private SimpleTask<Boolean> validate_get() {
        return new SimpleTask<Boolean>() {

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
    }

    private SimpleTask<MinecraftAccount> refresh_get() {
        return new SimpleTask<MinecraftAccount>() {

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
                        progress.scheduleTask(saveAccounts_get());
                    }
                    Objects.requireNonNull(getSelectedAccount());
                }
            }

        };
    }

}
