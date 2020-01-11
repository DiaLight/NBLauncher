package dialight.nblauncher;

import dialight.javafx.ErrorDialog;
import dialight.mvc.MVCApplication;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.AccountsController;
import dialight.nblauncher.controller.LauncherController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main extends Application {

    MVCApplication launcher = new MVCApplication();

    private static void showError(Thread t, Throwable e) {
        if (Platform.isFxApplicationThread()) {
            ErrorDialog.show(e);
        } else {
            System.err.println("An unexpected error occurred in " + t);
        }
    }


    @Override public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(Main::showError);
        launcher.registerController(new ProgressController());
        launcher.registerController(new LauncherController());
        launcher.registerController(new AccountsController());
        launcher.registerController(new SceneController(primaryStage));
        launcher.registerDone();

        SceneController sceneCtl = launcher.findController(SceneController.class);
        AccountsController accountsCtl = launcher.findController(AccountsController.class);

        primaryStage.setScene(sceneCtl.getMainScene());
        sceneCtl.initLogic(launcher);

        accountsCtl.loadAccountsSync();
        if (accountsCtl.getSelectedAccount() == null) {
            sceneCtl.gotoAddAccount();
        } else {
            sceneCtl.gotoMain();
        }

        primaryStage.setMinWidth(primaryStage.getScene().getWidth());
        primaryStage.setMinHeight(primaryStage.getScene().getHeight());
        InputStream iconStream = Main.class.getResourceAsStream("icon.png");
        primaryStage.getIcons().add(new Image(iconStream));
        primaryStage.show();

        if(getParameters().getUnnamed().contains("--debug-structure")) {
            ViewDebug.tryRunScenicView(sceneCtl.getMainScene());
        }
        if(getParameters().getUnnamed().contains("--debug-css")) {
            ViewDebug.tryRunCssfx(sceneCtl.getMainScene());
        }
    }

    @Override public void stop() throws Exception {
        launcher.findController(AccountsController.class).saveAccountsSync();
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
