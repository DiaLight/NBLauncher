package dialight.nblauncher.view;

import dialight.javafx.ListSingleSelectBidirectionalBinding;
import dialight.minecraft.MinecraftAccount;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.AccountsController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccountsView extends View {

    private final Parent root = createFromFxml();

    private final Button backButton = findById("back_button");

    private final ListView<MinecraftAccount> accountsList = findById("profile_list");
    private final Button addAccountButton = findById("goto_add_account");

    @Override public void initLogic(MVCApplication app) {
        AccountsController accountsCtl = app.findController(AccountsController.class);
        SceneController sceneCtl = app.findController(SceneController.class);

        backButton.visibleProperty().bind(accountsCtl.hasAccountProperty());
        backButton.setOnAction(event -> {
            sceneCtl.gotoPrev();
        });

        Function<MinecraftAccount, ContextMenu> contextMenuFunction = minecraftProfile -> {
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", minecraftProfile.getName()));
            deleteItem.setOnAction(event -> accountsCtl.delete(minecraftProfile));

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().add(deleteItem);
            return contextMenu;
        };

        accountsList.setCellFactory(listView -> new ListCell<MinecraftAccount>() {
            @Override protected void updateItem(MinecraftAccount item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item.getName());
                    setContextMenu(contextMenuFunction.apply(item));
                } else {
                    setGraphic(null);
                    setText(null);
                    setContextMenu(null);
                }
            }
        });
        accountsList.setItems(accountsCtl.getAccounts());

        new ListSingleSelectBidirectionalBinding<>(accountsList, accountsCtl.selectedAccountProperty()).bind();

        addAccountButton.setOnAction(e -> {
            sceneCtl.gotoAddAccount();
        });
    }

    @Override public Parent getRoot() {
        return root;
    }

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add("--view-class=" + AccountsView.class.getName());
        argsList.addAll(Arrays.asList("--width=400", "--height=200"));
        argsList.add("--debug");
        Application.launch(ViewDebug.class, argsList.toArray(new String[0]));
    }

}
