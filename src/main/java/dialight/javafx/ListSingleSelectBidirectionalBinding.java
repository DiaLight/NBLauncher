package dialight.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.util.stream.Collectors;

public class ListSingleSelectBidirectionalBinding<T> {

    private final ListView<T> list;
    private final ObjectProperty<T> property;
    boolean lock = false;

    public ListSingleSelectBidirectionalBinding(ListView<T> list, ObjectProperty<T> property) {
        this.list = list;
        this.property = property;
    }

    public void bind() {
        list.getSelectionModel().selectedItemProperty().addListener(this::onListSelectChange);
        property.addListener(this::onCtlSelectedChange);
        selectInList(property.getValue());
    }

    private void onListSelectChange(ObservableValue<? extends T> observableValue, T oldValue, T selected) {
//                System.out.println("list -> selected " + selected + " " + lock);
        if(lock) return;
        lock = true;
        property.setValue(selected);
        lock = false;
    }

    private void onCtlSelectedChange(ObservableValue<? extends T> observableValue, T oldValue, T selected) {
//                System.out.println("selected -> list " + selected + " " + lock);
        if(lock) return;
        lock = true;
        selectInList(selected);
        lock = false;
    }

    private void selectInList(T selected) {
        ObservableList<T> items = list.getItems();
        if(items.isEmpty()) return;
        for (int i = 0; i < items.size(); i++) {
            T account = items.get(i);
            if(account.equals(selected)) {
                list.getSelectionModel().select(i);
                return;
            }
        }
        throw new IllegalStateException(selected + " not found in " + items.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]")));
    }

}
