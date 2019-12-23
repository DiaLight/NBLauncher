package dialight.nblauncher.controller;

import dialight.misc.FileUtils;
import dialight.mvc.Controller;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.nblauncher.view.Common;
import dialight.nblauncher.view.LauncherView;
import dialight.nblauncher.view.ProfilesView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class SceneController extends Controller {

    private final Stage stage;
    private Common common = new Common();
    private View mainView = new LauncherView();
    private View profilesView = new ProfilesView();
    private Scene mainScene = new Scene(common.getRoot(), 600, 400);

    public SceneController(Stage stage) {
        this.stage = stage;
    }

    public Scene getMainScene() {
        return mainScene;
    }

    public void gotoProfiles() {
        common.setView(profilesView);
    }

    public void gotoMain() {
        common.setView(mainView);
    }

    public void initLogic(MVCApplication app) {

        Manifest manifest = FileUtils.getManifest();
        if(manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            stage.setTitle(attributes.getValue("Implementation-Title"));
        } else {
            stage.setTitle("test");
        }
        common.initLogic(app);
        mainView.initLogic(app);
        profilesView.initLogic(app);
    }

}
