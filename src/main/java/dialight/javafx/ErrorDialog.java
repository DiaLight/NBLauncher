package dialight.javafx;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorDialog {

    public static void show(Throwable ex) {
        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Упс, что-то пошло не так(");
        alert.setContentText(ex.getLocalizedMessage());
        alert.setResizable(true);
        alert.getDialogPane().setExpandableContent(new FXBuilder() {
            @Override public Node build() {
                return add(new GridPane(), root -> {
                    root.setMaxWidth(Double.MAX_VALUE);
                    add(new Label("Stacktrace:"), label -> {
                        GridPane.setConstraints(label, 0, 0);
                    });
                    add(new TextArea(exceptionText), textArea -> {
                        GridPane.setConstraints(textArea, 0, 1);
                        textArea.setEditable(false);
                        textArea.setWrapText(true);

                        textArea.setMaxWidth(Double.MAX_VALUE);
                        textArea.setMaxHeight(Double.MAX_VALUE);
                        GridPane.setVgrow(textArea, Priority.ALWAYS);
                        GridPane.setHgrow(textArea, Priority.ALWAYS);
                    });
                });
            }
        }.build());
        alert.getDialogPane().setMaxWidth(Double.MAX_VALUE);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

}
