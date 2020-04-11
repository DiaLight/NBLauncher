package dialight.nblauncher.view;

import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.AccountsController;
import dialight.nblauncher.controller.LauncherController;
import dialight.nblauncher.controller.SceneController;
import dialight.nblauncher.controller.SettingsController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsView extends View {

    private final Parent root = createFromFxml();

    private final Button backButton = findById("back_button");

    private final Button selectGameDirButton = findById("select_game_dir");
    private final TextField gameDirField = findById("game_dir");


    @Override public void initLogic(MVCApplication app) {
        SettingsController settingsCtl = app.findController(SettingsController.class);
        SceneController sceneCtl = app.findController(SceneController.class);

        backButton.setOnAction(event -> {
            if(settingsCtl.validate()) {
                settingsCtl.saveSettings(() -> {});
                sceneCtl.gotoPrev();
            }
        });

        gameDirField.textProperty().bindBidirectional(settingsCtl.gameDirProperty());

        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Some Directories");
        selectGameDirButton.setOnAction(event -> {
            if(Files.exists(settingsCtl.getGameDirPath())) {
                directoryChooser.setInitialDirectory(new File(settingsCtl.getGameDir()));
            } else {
                directoryChooser.setInitialDirectory(null);
            }

            File dir = directoryChooser.showDialog(sceneCtl.getStage());
            if (dir != null) {
                settingsCtl.setGameDir(dir.getAbsolutePath());
                settingsCtl.validateGameDir();
            }
        });

        settingsCtl.badGameDirProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                Color color = Color.rgb(255, 80, 80);
                BackgroundFill fill = new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY);
                gameDirField.setBackground(new Background(fill));
            } else {
                gameDirField.setBackground(null);
            }
        });
    }

    @Override public Parent getRoot() {
        return root;
    }

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add("--view-class=" + SettingsView.class.getName());
        argsList.addAll(Arrays.asList("--width=400", "--height=200"));
        argsList.add("--debug");
        Application.launch(ViewDebug.class, argsList.toArray(new String[0]));
    }

}
