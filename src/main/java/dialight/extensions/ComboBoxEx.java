package dialight.extensions;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ComboBoxEx<T> {
    
    private final ComboBox<T> comboBox;

    public ComboBoxEx(ComboBox<T> comboBox) {
        this.comboBox = comboBox;
    }

    public static <T> ComboBoxEx<T> of(ComboBox<T> listCell) {
        return new ComboBoxEx<T>(listCell);
    }

    @NotNull private ListCell<T> createFormatCell(Function<T, String> op) {
        return new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(op.apply(item));
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        };
    }

    public void formatCell(Function<T, String> op) {
        comboBox.setCellFactory(listView -> createFormatCell(op));
        comboBox.setButtonCell(createFormatCell(op));
    }

}
