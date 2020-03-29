package dialight.nblauncher.view;

import dialight.extensions.CollectionEx;
import dialight.javafx.NoSelectionModel;
import dialight.minecraft.MCVersion;
import dialight.minecraft.json.versions.GameType;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.mvc.ViewDebug;
import dialight.nblauncher.controller.AccountsController;
import dialight.nblauncher.controller.LauncherController;
import dialight.nblauncher.controller.ProgressController;
import dialight.nblauncher.controller.SceneController;
import dialight.nblauncher.view.launcher.GameTypeCell;
import dialight.nblauncher.view.launcher.ModifierCell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private final ComboBox<GameTypeCell> gameTypeList = findById("gametype_list");
    private final ComboBox<MCVersion> mcversionList = findById("mcversion_list");
    private final ComboBox<String> versionList = findById("version_list");

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
        versionList.valueProperty().bindBidirectional(launcherCtl.selectedVersionProperty());

        currentGameType.addListener((observable, oldValue, newValue) -> {
            GameType gameType = newValue != null ? launcherCtl.getGameType(newValue.getId()) : null;
            Map<MCVersion, List<String>> versionMap = gameType == null ? Collections.emptyMap() : gameType.collectVersionMap();
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
            Map<MCVersion, List<String>> versionMap = gameType == null ? Collections.emptyMap() : gameType.collectVersionMap();
            List<String> versions = versionMap.getOrDefault(newValue, Collections.emptyList());
            String selectedVersion = null;
            if(gameType != null && newValue != null) {
                launcherCtl.getGuiPersistence().putMcVersion(gameType.getId(), newValue.toString());
                selectedVersion = launcherCtl.getGuiPersistence().getVersionMap().get(gameType.getId() + "-" + newValue.toString());
            }
            if(selectedVersion == null) selectedVersion = CollectionEx.of(versions).firstOrNull();
            versionList.setItems(FXCollections.observableList(versions));
            launcherCtl.selectVersion(selectedVersion);
        });

        launcherCtl.selectedVersionProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) {
                versionModifiersLabel.setVisible(false);
                return;
            }
            GameTypeCell gameTypeCell = currentGameType.get();
            MCVersion mcVersion = currentMCVersion.get();
            if(gameTypeCell != null && mcVersion != null) {
                launcherCtl.getGuiPersistence().putVersion(gameTypeCell.getId() + "-" + mcVersion.toString(), newValue);
            }
            List<String> selectedModifiers = launcherCtl.getGuiPersistence().getModifiersMap().get(newValue);
            List<ModifierCell> modifiers = new ArrayList<>();
            for (String modifier : launcherCtl.getVersion(newValue).getModifiers()) {
                modifiers.add(new ModifierCell(modifier));
            }
            versionModifiersLabel.setVisible(!modifiers.isEmpty());
            versionModifiers.setItems(FXCollections.observableList(modifiers));
            if(selectedModifiers != null) {
                for (ModifierCell modifier : modifiers) {
                    modifier.setSelected(selectedModifiers.contains(modifier.getModifier()));
                }
            }
        });

        startButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> accountCtl.getSelectedAccount() != null, accountCtl.selectedAccountProperty()));

        startButton.setOnAction(e -> {
            List<String> modifiers = new ArrayList<>();
            for (ModifierCell item : versionModifiers.getItems()) {
                if(item.isSelected()) modifiers.add(item.getModifier());
            }
            launcherCtl.getGuiPersistence().putModifiers(launcherCtl.selectedVersion(), modifiers);
            launcherCtl.setModifiers(modifiers);
            launcherCtl.startMinecraft();
        });
        startButton.disableProperty().bind(progressCtl.busyProperty());
    }

    @Override public void save(MVCApplication app) {
        LauncherController launcherCtl = app.findController(LauncherController.class);
        List<String> modifiers = new ArrayList<>();
        for (ModifierCell item : versionModifiers.getItems()) {
            if(item.isSelected()) modifiers.add(item.getModifier());
        }
        launcherCtl.getGuiPersistence().putModifiers(launcherCtl.selectedVersion(), modifiers);
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
