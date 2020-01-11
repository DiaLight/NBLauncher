package dialight.nblauncher.view;

import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.nblauncher.controller.AccountsController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AddAccountView extends View {

    private final Parent root = createFromFxml();

    private final Button backButton = findById("back_button");

    private final TextField usernameText = findById("username");
    private final PasswordField passwordText = findById("password");
    private final Button loginButton = findById("login");

    @Override public void initLogic(MVCApplication app) {
        AccountsController accountsCtl = app.findController(AccountsController.class);
        SceneController sceneCtl = app.findController(SceneController.class);
        ProgressController progressCtl = app.findController(ProgressController.class);

        backButton.visibleProperty().bind(accountsCtl.hasAccountProperty());
        backButton.setOnAction(event -> {
            sceneCtl.gotoPrev();
        });

        usernameText.textProperty().bindBidirectional(accountsCtl.usernameProperty());
        passwordText.textProperty().bindBidirectional(accountsCtl.passwordProperty());
        loginButton.setOnAction(e -> {
            accountsCtl.authenticate().setOnSucceeded(e2 -> {
                sceneCtl.gotoPrev();
            });
        });
        loginButton.disableProperty().bind(progressCtl.busyProperty());


        usernameText.setOnAction(event -> {
            passwordText.requestFocus();
        });
        passwordText.setOnAction(e -> {
            accountsCtl.authenticate().setOnSucceeded(e2 -> {
                sceneCtl.gotoPrev();
            });
        });
    }

    @Override public Parent getRoot() {
        return root;
    }

}
