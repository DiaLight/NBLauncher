package dialight.nblauncher.view;

import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.nblauncher.controller.ProgressController;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

public class Common extends View {

    private final Parent root = createFromFxml();

    private final StackPane viewRoot = findById("view_root");
    private final ProgressBar progress = findById("progress");
    private final Label progressLabel = findById("progress_label");

    @Override public void initLogic(MVCApplication app) {
        ProgressController progressCtl = app.findController(ProgressController.class);

        progress.visibleProperty().bind(progressCtl.busyProperty());
        progressLabel.visibleProperty().bind(progressCtl.busyProperty());
        progress.progressProperty().bind(progressCtl.progressProperty());
        progressLabel.textProperty().bind(progressCtl.statusProperty());
    }

    @Override public Parent getRoot() {
        return root;
    }

    public void setView(View view) {
        viewRoot.getChildren().clear();
        viewRoot.getChildren().add(view.getRoot());
    }

}
