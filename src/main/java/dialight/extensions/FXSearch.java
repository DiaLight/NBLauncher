package dialight.extensions;

import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Predicate;

public class FXSearch {

    @Nullable public static Node findFirstBy(Node root, Predicate<Node> iterator) {
        LinkedList<Node> read = new LinkedList<>();
        LinkedList<Node> write = new LinkedList<>();
        read.add(root);
        while(!read.isEmpty()) {
            for (Node node : read) {
                if(iterator.test(node)) return node;
                if(node instanceof Pane) {
                    Pane pane = (Pane) node;
                    write.addAll(pane.getChildren());
                } else if(node instanceof SplitPane) {
                    SplitPane pane = (SplitPane) node;
                    write.addAll(pane.getItems());
                } else {
//                    System.out.println("  " + node.getClass().getSimpleName());
                }
            }
            read.clear();
            LinkedList<Node> tmp = write;
            write = read;
            read = tmp;
        }
        return null;
    }

    @Nullable public static Node findFirstById(Node root, String id) {
        return findFirstBy(root, node -> Objects.equals(node.getId(), id));
    }
    @Nullable public static Node findFirstByClass(Node root, Class<? extends Node> cls) {
        return findFirstBy(root, node -> cls.equals(node.getClass()));
    }

}
