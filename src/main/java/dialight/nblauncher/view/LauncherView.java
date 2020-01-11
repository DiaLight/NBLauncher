package dialight.nblauncher.view;

import dialight.extensions.CollectionEx;
import dialight.javafx.NoSelectionModel;
import dialight.minecraft.MCVersion;
import dialight.minecraft.json.versions.Profile;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.AccountsController;
import dialight.nblauncher.controller.LauncherController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.util.*;
import java.util.stream.Collectors;

public class LauncherView extends View {

    private final Parent root = createFromFxml();

    private final Label accountName = findById("account_name");
    private final Button settingsButton = findById("settings_button");

    private final ComboBox<String> profileList = findById("profile_list");
    private final ComboBox<MCVersion> mcversionList = findById("mcversion_list");
    private final ComboBox<String> versionList = findById("version_list");

    private final Button startButton = findById("start_button");

    private final Label versionModifiersLabel = findById("version_modifiers_label");
    private final ListView<String> versionModifiers = findById("version_modifiers");

    private final ObjectProperty<String> currentProfile = new SimpleObjectProperty<>(null);
    private final ObjectProperty<MCVersion> currentMCVersion = new SimpleObjectProperty<>(null);

    {
        versionModifiers.setSelectionModel(new NoSelectionModel<>());
        versionModifiers.setCellFactory(view -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (!empty && item != null) {
                    CheckBox checkBox = new CheckBox(item);
                    checkBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                    setGraphic(checkBox);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    @Override public void initLogic(MVCApplication app) {
        LauncherController launcherCtl = app.findController(LauncherController.class);
        AccountsController accountCtl = app.findController(AccountsController.class);
        SceneController sceneCtl = app.findController(SceneController.class);
        ProgressController progressCtl = app.findController(ProgressController.class);

        accountCtl.selectedAccountProperty().addListener((observable, oldValue, newValue) -> accountName.setText(newValue == null ? "" : newValue.getName()));
        {
            ContextMenu contextMenu = new ContextMenu();
            settingsButton.setOnAction(e -> {
                contextMenu.show(settingsButton, Side.RIGHT, 0, 0);
            });

            {
                MenuItem accounts = new MenuItem("Аккаунты");
                accounts.setOnAction(event -> sceneCtl.gotoAccounts());
                contextMenu.getItems().add(accounts);
            }

            {
                MenuItem item = new MenuItem("Обновить конфиги с сервера");
                item.setOnAction(event -> launcherCtl.loadOnlineVersions());
                contextMenu.getItems().add(item);
                item.disableProperty().bind(progressCtl.busyProperty());
            }
        }

        launcherCtl.profilesProperty().addListener((observable, oldValue, newValue) -> {
            Profile profile = CollectionEx.of(newValue).firstOrNull();
            if(profile == null) {
                // TODO: goto update config page
                throw new IllegalStateException("TODO: goto update config page");
            }
            profileList.setItems(FXCollections.observableList(newValue.stream().map(Profile::getId).collect(Collectors.toList())));
            currentProfile.setValue(profile.getId());
        });
        profileList.valueProperty().bindBidirectional(currentProfile);
        mcversionList.valueProperty().bindBidirectional(currentMCVersion);
        versionList.valueProperty().bindBidirectional(launcherCtl.selectedVersionProperty());

        currentProfile.addListener((observable, oldValue, newValue) -> {
            Profile profile = launcherCtl.getProfile(newValue);
            Map<MCVersion, List<String>> versionMap = profile == null ? Collections.emptyMap() : profile.collectVersionMap();
            ArrayList<MCVersion> mcVersions = new ArrayList<>(versionMap.keySet());
            mcVersions.sort((o1, o2) -> -o1.compareTo(o2));
            mcversionList.setItems(FXCollections.observableList(mcVersions));
            MCVersion mcVersion = CollectionEx.of(mcVersions).firstOrNull();
            currentMCVersion.setValue(mcVersion);
        });

        currentMCVersion.addListener((observable, oldValue, newValue) -> {
            Profile profile = launcherCtl.getProfile(currentProfile.get());
            Map<MCVersion, List<String>> versionMap = profile == null ? Collections.emptyMap() : profile.collectVersionMap();
            List<String> versions = versionMap.getOrDefault(newValue, Collections.emptyList());
            versionList.setItems(FXCollections.observableList(versions));
            launcherCtl.selectVersion(CollectionEx.of(versions).firstOrNull());
        });

        launcherCtl.selectedVersionProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) {
                versionModifiersLabel.setVisible(false);
                return;
            }
            List<String> modifiers = launcherCtl.getVersion(newValue).getModifiers();
            versionModifiersLabel.setVisible(!modifiers.isEmpty());
            versionModifiers.setItems(FXCollections.observableList(modifiers));
        });

        startButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> accountCtl.getSelectedAccount() != null, accountCtl.selectedAccountProperty()));

        startButton.setOnAction(e -> {
            List<String> modifiers = new ArrayList<>();
            for (Node node : versionModifiers.lookupAll(".list-cell:filled .check-box")) {
                CheckBox checkBox = (CheckBox) node;
                if(checkBox.isSelected()) modifiers.add(checkBox.getText());
            }
            launcherCtl.setModifiers(modifiers);
            launcherCtl.startMinecraft();
        });
        startButton.disableProperty().bind(progressCtl.busyProperty());

    }

    @Override public Parent getRoot() {
        return root;
    }

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add("--view-class=" + AccountsView.class.getName());
        Application.launch(ViewDebug.class, argsList.toArray(new String[0]));
    }

}
