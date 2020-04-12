package dialight.nblauncher.view.upgrade;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;

public class AssetCell {

    private final HBox root = new HBox();
    private final String url;
    private final Application app;
    private final Button hyperlink;

    public AssetCell(String name, long size, String url, Application app) {
        this.url = url;
        this.app = app;
        root.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(new Label(name + ": "));
        hyperlink = new Button("download");
        hyperlink.setOnAction(this::onAction);
        {
            ContextMenu menu = new ContextMenu();
            {
                MenuItem item = new MenuItem("copy");
                item.setOnAction(this::onCopy);
                menu.getItems().add(item);
            }
            hyperlink.setContextMenu(menu);
        }
        root.getChildren().add(hyperlink);
    }

    private void onCopy(ActionEvent event) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(url);
        clipboard.setContent(content);
    }

    private void onAction(ActionEvent event) {
        app.getHostServices().showDocument(url);
    }

    public Node getGraphic() {
        return root;
    }

}
