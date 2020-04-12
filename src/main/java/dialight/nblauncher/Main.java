package dialight.nblauncher;

import dialight.minecraft.MinecraftRepo;
import dialight.minecraft.json.versions.Versions;
import dialight.misc.FileUtils;
import dialight.misc.HttpRequest;
import dialight.misc.Json;
import dialight.mvc.MVCApplication;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.*;
import dialight.nblauncher.json.GithubRelease;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Main extends Application {

    MVCApplication launcher = new MVCApplication();
    private NBLauncher nbl;
    private PrintWriter exceptionsWriter;

    private void showError(Thread t, Throwable e) {
        if (Platform.isFxApplicationThread()) {
            if(exceptionsWriter == null) {
                try {
                    exceptionsWriter = new PrintWriter(Files.newBufferedWriter(nbl.nblPaths.exceptionsFile));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            e.printStackTrace(exceptionsWriter);
            exceptionsWriter.flush();
        } else {
            System.err.println("An unexpected error occurred in " + t);
        }
    }


    @Override public void start(Stage primaryStage) throws Exception {
        nbl = new NBLauncher(MinecraftRepo.getMinecraftPath());
        Thread.setDefaultUncaughtExceptionHandler(this::showError);

        launcher.registerController(new ProgressController());
        launcher.registerController(new UpgradeController(nbl));
        launcher.registerController(new LauncherController(nbl));
        launcher.registerController(new SettingsController(nbl));
        launcher.registerController(new AccountsController(nbl));
        launcher.registerController(new SceneController(this, primaryStage));

        SceneController sceneCtl = launcher.findController(SceneController.class);
        AccountsController accountsCtl = launcher.findController(AccountsController.class);
        LauncherController launcherCtl = launcher.findController(LauncherController.class);
        UpgradeController upgradeCtl = launcher.findController(UpgradeController.class);

        primaryStage.setScene(sceneCtl.getMainScene());
        sceneCtl.initLogic(launcher);

        primaryStage.setMinWidth(primaryStage.getScene().getWidth());
        primaryStage.setMinHeight(primaryStage.getScene().getHeight());
        InputStream iconStream = Main.class.getResourceAsStream("icon.png");
        primaryStage.getIcons().add(new Image(iconStream));
        primaryStage.show();

        launcher.fireInit(() -> {
            if (accountsCtl.getSelectedAccount() == null) {
                sceneCtl.gotoAddAccount();
            } else {
                if (upgradeCtl.isLatest()) {
                    sceneCtl.gotoMain();
                } else {
                    sceneCtl.gotoUpgrade();
                }
            }
            launcherCtl.loadOnlineVersions(() -> {});
        });

        if(getParameters().getUnnamed().contains("--debug-structure")) {
            ViewDebug.tryRunScenicView(sceneCtl.getMainScene());
        }
        if(getParameters().getUnnamed().contains("--debug-css")) {
            ViewDebug.tryRunCssfx(sceneCtl.getMainScene());
        }
    }

    @Override public void stop() throws Exception {
        SceneController sceneCtl = launcher.findController(SceneController.class);
        sceneCtl.save(launcher);
        launcher.findController(SettingsController.class).saveSettingsSync();
        launcher.findController(AccountsController.class).saveAccountsSync();
        launcher.findController(LauncherController.class).save();
        if(exceptionsWriter != null) exceptionsWriter.close();
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
