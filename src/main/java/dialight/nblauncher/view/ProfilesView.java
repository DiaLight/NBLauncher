package dialight.nblauncher.view;

import dialight.extensions.ComboBoxEx;
import dialight.extensions.ListViewEx;
import dialight.minecraft.MinecraftProfile;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.ProfileController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ProfilesView extends View {

    private final Parent root = createFromFxml();

    private final ListView<MinecraftProfile> profileList = findById("profile_list");
    private final Button homeButton = findById("goto_home");

    private final TextField usernameText = findById("username");
    private final PasswordField passwordText = findById("password");
    private final Button loginButton = findById("login");

    @Override public void initLogic(MVCApplication app) {
        ProfileController profileCtl = app.findController(ProfileController.class);
        SceneController sceneCtl = app.findController(SceneController.class);
        ProgressController progressCtl = app.findController(ProgressController.class);

        Function<MinecraftProfile, ContextMenu> contextMenuFunction = minecraftProfile -> {
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", minecraftProfile.getName()));
            deleteItem.setOnAction(event -> profileCtl.delete(minecraftProfile));

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().add(deleteItem);
            return contextMenu;
        };

        profileList.setCellFactory(listView -> new ListCell<MinecraftProfile>() {
            @Override protected void updateItem(MinecraftProfile item, boolean empty) {
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


        profileList.setItems(profileCtl.getProfiles());
        homeButton.setOnAction(e -> {
            sceneCtl.gotoMain();
        });

        usernameText.textProperty().bindBidirectional(profileCtl.usernameProperty());
        passwordText.textProperty().bindBidirectional(profileCtl.passwordProperty());
        loginButton.setOnAction(e -> {
            profileCtl.authenticate();
        });
        loginButton.disableProperty().bind(progressCtl.busyProperty());
    }

    @Override public Parent getRoot() {
        return root;
    }

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add("--view-class=" + ProfilesView.class.getName());
        argsList.addAll(Arrays.asList("--width=400", "--height=200"));
        argsList.add("--debug");
        Application.launch(ViewDebug.class, argsList.toArray(new String[0]));
    }

}
