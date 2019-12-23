package dialight.extensions;

import javafx.scene.control.ListCell;

public class ListCellEx<T> {

    private final ListCell<T> listCell;

    public ListCellEx(ListCell<T> listCell) {
        this.listCell = listCell;
    }

    public static <T> ListCellEx<T> of(ListCell<T> listCell) {
        return new ListCellEx<T>(listCell);
    }

}
