package dialight.extensions;

import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ListViewEx<T> {
    
    private final ListView<T> listView;

    public ListViewEx(ListView<T> listView) {
        this.listView = listView;
    }

    public static <T> ListViewEx<T> of(ListView<T> listCell) {
        return new ListViewEx<T>(listCell);
    }

    @NotNull public static <T> ListCell<T> createFormatCell(Function<T, String> op) {
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
        listView.setCellFactory(listView -> createFormatCell(op));
    }

}
