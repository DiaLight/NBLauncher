package dialight.mvc;

import com.sun.javafx.application.LauncherImpl;
import dialight.extensions.FXSearch;
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

    public void save(MVCApplication app) {}

    public abstract Parent getRoot();

    public <T extends Node> T findById(String id) {
        Node node = FXSearch.findFirstById(getRoot(), id);
        if(node == null) throw new IllegalStateException("element with id " + id + " not found");
        return (T) node;
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
