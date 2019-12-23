package dialight.mvc;

import com.sun.javafx.application.LauncherImpl;
import dialight.nblauncher.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class View {

    public abstract void initLogic(MVCApplication app);

    public abstract Parent getRoot();

    public <T extends Node> T findById(String id) {
        LinkedList<Node> read = new LinkedList<>();
        LinkedList<Node> write = new LinkedList<>();
        read.add(getRoot());
        while(!read.isEmpty()) {
            for (Node node : read) {
//                System.out.println(node.getId());
                if(Objects.equals(node.getId(), id)) return (T) node;
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
        throw new IllegalStateException("element with id " + id + " not found");
    }

    protected Parent createFromFxml() {
        try {
            Class<? extends View> cls = getClass();
            return FXMLLoader.load(cls.getResource(cls.getSimpleName() + ".fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
