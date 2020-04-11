package dialight.nblauncher.view;

import dialight.extensions.CollectionEx;
import dialight.javafx.NoSelectionModel;
import dialight.minecraft.MCPaths;
import dialight.minecraft.MCVersion;
import dialight.minecraft.json.versions.DisplayEntry;
import dialight.minecraft.json.versions.GameType;
import dialight.minecraft.json.versions.Version;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.*;
import dialight.nblauncher.view.launcher.GameTypeCell;
import dialight.nblauncher.view.launcher.ModifierCell;
import dialight.nblauncher.view.launcher.VersionCell;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LauncherView extends View {

    private final Parent root = createFromFxml();

    private final Label accountName = findById("account_name");
    private final Button settingsButton = findById("settings_button");

    private final ComboBox<GameTypeCell> gameTypeList = findById("gametype_list");
    private final ComboBox<MCVersion> mcversionList = findById("mcversion_list");
    private final ComboBox<VersionCell> versionList = findById("version_list");

    private final Button startButton = findById("start_button");

    private final Label versionModifiersLabel = findById("version_modifiers_label");
    private final ListView<ModifierCell> versionModifiers = findById("version_modifiers");

    private final ObjectProperty<GameTypeCell> currentGameType = new SimpleObjectProperty<>(null);
    private final ObjectProperty<MCVersion> currentMCVersion = new SimpleObjectProperty<>(null);

    {
        versionModifiers.setSelectionModel(new NoSelectionModel<>());
        versionModifiers.setCellFactory(view -> new ListCell<ModifierCell>() {
            @Override
            protected void updateItem(ModifierCell item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (!empty && item != null) {
                    setGraphic(item.getGraphic());
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    @Override public void initLogic(MVCApplication app) {
        LauncherController launcherCtl = app.findController(LauncherController.class);
        AccountsController accountCtl = app.findController(AccountsController.class);
        SettingsController settingsCtl = app.findController(SettingsController.class);
        SceneController sceneCtl = app.findController(SceneController.class);
        ProgressController progressCtl = app.findController(ProgressController.class);

        accountCtl.selectedAccountProperty().addListener((observable, oldValue, newValue) -> accountName.setText(newValue == null ? "" : newValue.getName()));
        {
            ContextMenu contextMenu = new ContextMenu();
            settingsButton.setOnAction(e -> {
                contextMenu.show(settingsButton, Side.RIGHT, 0, 0);
            });

            {
                MenuItem item = new MenuItem("Аккаунты");
                item.setOnAction(event -> sceneCtl.gotoAccounts());
                contextMenu.getItems().add(item);
            }

            {
                MenuItem item = new MenuItem("Настрйки");
                item.setOnAction(event -> sceneCtl.gotoSettings());
                contextMenu.getItems().add(item);
            }

            {
                MenuItem item = new MenuItem("Обновить");
                item.setOnAction(event -> launcherCtl.loadOnlineVersions(() -> {}));
                contextMenu.getItems().add(item);
                item.disableProperty().bind(progressCtl.busyProperty());
            }
        }

        launcherCtl.gameTypesProperty().addListener((observable, oldValue, newValue) -> {
            String selectedGameTypeId = launcherCtl.getGuiPersistence().getGameTypeId();
            List<GameTypeCell> gameTypeCells = new ArrayList<>();
            GameTypeCell selected = null;
            for (GameType gameType : newValue) {
                GameTypeCell gameTypeCell = new GameTypeCell(gameType.getId(), gameType.getDisplayName());
                if(gameType.getId().equals(selectedGameTypeId)) {
                    selected = gameTypeCell;
                }
                gameTypeCells.add(gameTypeCell);
            }
            if(selected == null) selected = CollectionEx.of(gameTypeCells).firstOrNull();
            gameTypeList.setItems(FXCollections.observableList(gameTypeCells));
            currentGameType.setValue(selected);
        });
        gameTypeList.valueProperty().bindBidirectional(currentGameType);
        mcversionList.valueProperty().bindBidirectional(currentMCVersion);

        currentGameType.addListener((observable, oldValue, newValue) -> {
            GameType gameType = newValue != null ? launcherCtl.getGameType(newValue.getId()) : null;
            Map<MCVersion, List<DisplayEntry>> versionMap = gameType == null ? Collections.emptyMap() : gameType.collectVersionMap();
            ArrayList<MCVersion> mcVersions = new ArrayList<>(versionMap.keySet());
            mcVersions.sort((o1, o2) -> -o1.compareTo(o2));
            String selectedVersion = null;
            if(gameType != null) {
                launcherCtl.getGuiPersistence().setGameTypeId(gameType.getId());
                selectedVersion = launcherCtl.getGuiPersistence().getMcVersionMap().get(gameType.getId());
            }
            MCVersion selected = null;
            for (MCVersion mcVersion : mcVersions) {
                if(mcVersion.toString().equals(selectedVersion)) {
                    selected = mcVersion;
                }
            }
            if(selected == null) selected = CollectionEx.of(mcVersions).firstOrNull();
            mcversionList.setItems(FXCollections.observableList(mcVersions));
            currentMCVersion.setValue(selected);
        });

        currentMCVersion.addListener((observable, oldValue, newValue) -> {
            GameTypeCell gameTypeCell = currentGameType.get();
            GameType gameType = gameTypeCell != null ? launcherCtl.getGameType(gameTypeCell.getId()) : null;
            Map<MCVersion, List<DisplayEntry>> versionMap = gameType == null ? Collections.emptyMap() : gameType.collectVersionMap();
            List<DisplayEntry> versions = versionMap.getOrDefault(newValue, Collections.emptyList());
            String selectedVersion = null;
            if(gameType != null && newValue != null) {
                launcherCtl.getGuiPersistence().putMcVersion(gameType.getId(), newValue.toString());
                selectedVersion = launcherCtl.getGuiPersistence().getVersionMap().get(gameType.getId() + "-" + newValue.toString());
            }
            VersionCell selected = null;
            List<VersionCell> items = new ArrayList<>();
            for (DisplayEntry version : versions) {
                VersionCell cell = new VersionCell(version.getId(), version.getDisplayName());
                if(version.getId().equals(selectedVersion)) selected = cell;
                items.add(cell);
            }
            if(selected == null) selected = CollectionEx.of(items).firstOrNull();
            versionList.setItems(FXCollections.observableList(items));
            versionList.setValue(selected);
        });

        versionList.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) {
                versionModifiersLabel.setVisible(false);
                return;
            }
            GameTypeCell gameTypeCell = currentGameType.get();
            MCVersion mcVersion = currentMCVersion.get();
            if(gameTypeCell != null && mcVersion != null) {
                launcherCtl.getGuiPersistence().putVersion(gameTypeCell.getId() + "-" + mcVersion.toString(), newValue.getId());
            }
            List<String> selectedModifiers = launcherCtl.getGuiPersistence().getModifiersMap().get(newValue.getId());
            List<ModifierCell> modifiers = new ArrayList<>();
            Version version = launcherCtl.getVersion(newValue.getId());
            if(version == null) throw new NullPointerException("version " + newValue + " not found");
            for (DisplayEntry modifier : version.getModifiers()) {
                modifiers.add(new ModifierCell(modifier.getId(), modifier.getDisplayName()));
            }
            versionModifiersLabel.setVisible(!modifiers.isEmpty());
            versionModifiers.setItems(FXCollections.observableList(modifiers));
            if(selectedModifiers != null) {
                for (ModifierCell modifier : modifiers) {
                    modifier.setSelected(selectedModifiers.contains(modifier.getId()));
                }
            }
        });

        startButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> accountCtl.getSelectedAccount() != null, accountCtl.selectedAccountProperty()));

        startButton.setOnAction(e -> {
            VersionCell version = versionList.getValue();
            if (version == null) return;
            List<String> modifiers = new ArrayList<>();
            for (ModifierCell item : versionModifiers.getItems()) {
                if(item.isSelected()) modifiers.add(item.getId());
            }
            launcherCtl.getGuiPersistence().putModifiers(version.getId(), modifiers);
            launcherCtl.setModifiers(modifiers);

            if(settingsCtl.validate()) {
                launcherCtl.startMinecraft(version.getId(), settingsCtl.getGameDirPath());
            } else {
                sceneCtl.gotoSettings();
            }
        });
        startButton.disableProperty().bind(progressCtl.busyProperty());
    }

    @Override public void save(MVCApplication app) {
        LauncherController launcherCtl = app.findController(LauncherController.class);
        List<String> modifiers = new ArrayList<>();
        for (ModifierCell item : versionModifiers.getItems()) {
            if(item.isSelected()) modifiers.add(item.getId());
        }
        launcherCtl.getGuiPersistence().putModifiers(versionList.getValue().getId(), modifiers);
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
