package dialight.nblauncher;

import dialight.mvc.MVCApplication;
import dialight.nblauncher.controller.LauncherController;
import dialight.nblauncher.controller.ProfileController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {

    @Override public void start(Stage primaryStage) throws Exception {
        MVCApplication launcher = new MVCApplication();

        launcher.registerController(new ProgressController());
        launcher.registerController(new LauncherController());
        launcher.registerController(new ProfileController());
        launcher.registerController(new SceneController(primaryStage));
        launcher.registerDone();

        SceneController sceneCtl = launcher.findController(SceneController.class);
        primaryStage.setScene(sceneCtl.getMainScene());
        sceneCtl.gotoMain();
        sceneCtl.initLogic(launcher);
        primaryStage.setMinWidth(primaryStage.getScene().getWidth());
        primaryStage.setMinHeight(primaryStage.getScene().getHeight());
        InputStream iconStream = Main.class.getResourceAsStream("icon.png");
        primaryStage.getIcons().add(new Image(iconStream));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

}
