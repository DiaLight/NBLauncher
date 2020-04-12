package dialight.nblauncher.view;

import dialight.javafx.NoSelectionModel;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.nblauncher.controller.SceneController;
import dialight.nblauncher.controller.SettingsController;
import dialight.nblauncher.controller.UpgradeController;
import dialight.nblauncher.view.launcher.ModifierCell;
import dialight.nblauncher.view.upgrade.AssetCell;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.web.WebView;

public class UpgradeView extends View {

    private final Parent root = createFromFxml();

    private final ListView<AssetCell> assets = findById("assets");

    {
        assets.setSelectionModel(new NoSelectionModel<>());
        assets.setCellFactory(view -> new ListCell<AssetCell>() {
            @Override
            protected void updateItem(AssetCell item, boolean empty) {
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
        UpgradeController upgradeCtl = app.findController(UpgradeController.class);
        SceneController sceneCtl = app.findController(SceneController.class);

        assets.setItems(upgradeCtl.getAssets());

    }

    @Override public Parent getRoot() {
        return root;
    }

}
