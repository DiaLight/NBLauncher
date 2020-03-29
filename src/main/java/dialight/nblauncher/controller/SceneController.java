package dialight.nblauncher.controller;

import dialight.misc.FileUtils;
import dialight.mvc.Controller;
import dialight.mvc.MVCApplication;
import dialight.mvc.View;
import dialight.nblauncher.view.AddAccountView;
import dialight.nblauncher.view.Common;
import dialight.nblauncher.view.LauncherView;
import dialight.nblauncher.view.AccountsView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class SceneController extends Controller {

    private final Stage stage;
    private final Common common = new Common();
    private final View mainView = new LauncherView();
    private final View accountsView = new AccountsView();
    private final View addAccountView = new AddAccountView();
    private final Scene mainScene = new Scene(common.getRoot(), 600, 400);
    private final LinkedList<View> viewStack = new LinkedList<>();
    private final BooleanProperty hasPrev = new SimpleBooleanProperty(false);

    public SceneController(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public Common getCommon() {
        return common;
    }

    public Scene getMainScene() {
        return mainScene;
    }

    public void gotoAccounts() {
        gotoView(accountsView);
    }
    public void gotoAddAccount() {
        gotoView(addAccountView);
    }
    public void gotoMain() {
        gotoView(mainView);
    }
    public void gotoPrev() {
        View prev = getPrev();
        if(prev == null) {
            if(getCurrent() == null || getCurrent() != mainView) gotoView(mainView);
            return;
        }
        gotoView(prev);
    }
    @Nullable private View getPrev() {
        Iterator<View> iterator = viewStack.descendingIterator();

        if (!iterator.hasNext()) return null;
        View current = iterator.next();

        if (!iterator.hasNext()) return null;
        View prev = iterator.next();

        return prev;
    }
    @Nullable private View getCurrent() {
        Iterator<View> iterator = viewStack.descendingIterator();

        if (!iterator.hasNext()) return null;
        View current = iterator.next();

        return current;
    }

    private void gotoView(View view) {
        if (stripTailTo(view)) {
            hasPrev.setValue(getPrev() != null);
        } else {
            viewStack.addLast(view);
            hasPrev.setValue(true);
        }
        common.setView(view);

    }
    public boolean stripTailTo(View view) {
        Iterator<View> iterator = viewStack.iterator();
        boolean found = false;
        while (iterator.hasNext()) {
            View next = iterator.next();
            if(found) {
                iterator.remove();
                continue;
            }
            if (view == next) {
                found = true;
            }
        }
        return found;
    }

    public void initLogic(MVCApplication app) {
        Manifest manifest = FileUtils.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String launcher_name = attributes.getValue("Implementation-Title");
        if(launcher_name == null) launcher_name = "test";
        String launcher_version = attributes.getValue("Implementation-Version");
        if(launcher_version == null) launcher_version = "0.0";
        stage.setTitle(launcher_name + " v" + launcher_version);

        common.initLogic(app);
        mainView.initLogic(app);
        accountsView.initLogic(app);
        addAccountView.initLogic(app);

        mainView.getRoot().getStylesheets().clear();
        accountsView.getRoot().getStylesheets().clear();
        addAccountView.getRoot().getStylesheets().clear();

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, t -> {
            if(t.getCode() == KeyCode.ESCAPE) {
                gotoPrev();
            }
        });
    }
    public void save(MVCApplication app) {
        common.save(app);
        mainView.save(app);
        accountsView.save(app);
        addAccountView.save(app);
    }

    public boolean hasPrev() {
        return hasPrev.get();
    }

    public BooleanProperty hasPrevProperty() {
        return hasPrev;
    }

}
