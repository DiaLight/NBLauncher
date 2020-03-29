package dialight.nblauncher.view;

import dialight.javafx.ErrorDialog;
import dialight.minecraft.ForbiddenOperationException;
import dialight.minecraft.MinecraftAccount;
import dialight.misc.SimpleTask;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.nblauncher.controller.AccountsController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.awt.*;
import java.util.concurrent.TimeUnit;

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
        Runnable startLogin = () -> {
            SimpleTask<MinecraftAccount> authenticate = accountsCtl.authenticate();
            authenticate.setOnSucceeded(e2 -> {
                sceneCtl.gotoPrev();
            });
            authenticate.setOnFailed(event -> {
                Throwable exception = authenticate.getException();
                if(exception instanceof ForbiddenOperationException) {
                    ForbiddenOperationException forbidden = (ForbiddenOperationException) exception;
                    if (forbidden.isInvalidUsernameOrPassword()) {
                        String error ="Непрвильный адрес электронной почты или пароль";
                        Tooltip tooltip = new Tooltip(error);
                        tooltip.setOnShown(e -> {
                            Scene scene = sceneCtl.getMainScene();
                            tooltip.setX(scene.getX() + scene.getWindow().getX() + scene.getWidth() - tooltip.getWidth());
                            tooltip.setY(scene.getY() + scene.getWindow().getY());
                        });
                        PauseTransition delay = new PauseTransition(Duration.seconds(3));
                        delay.setOnFinished(ee -> tooltip.hide());
                        tooltip.show(sceneCtl.getMainScene().getWindow());
                        delay.play();
                        return;
                    }
                }
                ErrorDialog.show(exception);
            });
        };
        loginButton.setOnAction(e -> startLogin.run());
        loginButton.disableProperty().bind(progressCtl.busyProperty());


        usernameText.setOnAction(event -> {
            passwordText.requestFocus();
        });
        passwordText.setOnAction(e -> startLogin.run());
    }

    @Override public Parent getRoot() {
        return root;
    }

}
