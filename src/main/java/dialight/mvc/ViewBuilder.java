package dialight.mvc;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

public abstract class ViewBuilder {

    private Node current;
    private Pane currentAsPane;

    public abstract Node build();

    private void setCurrent(Node node) {
        current = node;
        if(node instanceof Pane) {
            currentAsPane = (Pane) node;
        } else {
            currentAsPane = null;
        }
    }

    public <T extends Node> T add(T node, Consumer<T> op) {
        if(current != null) {
            if(currentAsPane == null) throw new IllegalStateException(node.getClass().getSimpleName() + " is not instance of Pane and can't have children");
            currentAsPane.getChildren().add(node);
        }
        setCurrent(node);
        op.accept(node);
        setCurrent(current.getParent());
        return node;
    }

    public void setId(String id) {
        current.setId(id);
    }

}
