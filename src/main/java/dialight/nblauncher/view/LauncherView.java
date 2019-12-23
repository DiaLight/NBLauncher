package dialight.nblauncher.view;

import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.ProfileController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.control.*;
import dialight.nblauncher.Instance;
import dialight.nblauncher.controller.LauncherController;
import dialight.extensions.ComboBoxEx;
import dialight.minecraft.MinecraftProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LauncherView extends View {

    private final Parent root = createFromFxml();

    private final ComboBox<MinecraftProfile> profileList = findById("profile_list");
    private final Button editProfilesButton = findById("edit_profiles_button");

    private final ComboBox<Instance> instanceList = findById("instance_list");
    private final Button instanceUpdateButton = findById("instances_update");

    private final Button startButton = findById("start_button");

    {
        ComboBoxEx.of(profileList).formatCell(MinecraftProfile::getName);
        ComboBoxEx.of(instanceList).formatCell(instance -> {
            return instance.getType() + " " + instance.getId();
        });
    }

    @Override public void initLogic(MVCApplication app) {
        LauncherController launcherCtl = app.findController(LauncherController.class);
        ProfileController profileCtl = app.findController(ProfileController.class);
        SceneController sceneCtl = app.findController(SceneController.class);
        ProgressController progressCtl = app.findController(ProgressController.class);

        profileList.setItems(profileCtl.getProfiles());
        profileList.valueProperty().bindBidirectional(profileCtl.selectedProfileProperty());
        editProfilesButton.setOnAction(e -> {
            sceneCtl.gotoProfiles();
        });

        instanceList.valueProperty().bindBidirectional(launcherCtl.selectedInstanceProperty());
        instanceList.setItems(launcherCtl.getInstances());
        instanceUpdateButton.setOnAction(e -> {
            launcherCtl.loadOnlineInstances();
        });
        instanceUpdateButton.disableProperty().bind(progressCtl.busyProperty());

        startButton.setOnAction(e -> {
            launcherCtl.startMinecraft();
        });
        startButton.disableProperty().bind(progressCtl.busyProperty());

    }

    @Override public Parent getRoot() {
        return root;
    }

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add("--view-class=" + ProfilesView.class.getName());
        Application.launch(ViewDebug.class, argsList.toArray(new String[0]));
    }

}
