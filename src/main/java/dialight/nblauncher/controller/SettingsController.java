package dialight.nblauncher.controller;


import dialight.misc.Json;
import dialight.misc.SimpleTask;
import dialight.misc.TextUtils;
import dialight.mvc.Controller;
import dialight.mvc.InitCtx;
import dialight.mvc.MVCApplication;
import dialight.nblauncher.NBLauncher;
import dialight.nblauncher.json.SettingsJson;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsController extends Controller {

    private final NBLauncher nbl;

    private final StringProperty gameDir;
    private final BooleanProperty badGameDir = new SimpleBooleanProperty(false);

    private ProgressController progress;

    public SettingsController(NBLauncher nbl) {
        this.nbl = nbl;
        gameDir = new SimpleStringProperty(nbl.homeDir.toString());
    }

    @Override protected void init(InitCtx ctx, MVCApplication app, Runnable done) {
        progress = app.findController(ProgressController.class);
        loadSettings(() -> {
            ctx.fireInit(app, done);
        });
    }

    public void setBadGameDir(boolean badGameDir) {
        this.badGameDir.set(badGameDir);
    }

    public boolean isBadGameDir() {
        return badGameDir.get();
    }

    public BooleanProperty badGameDirProperty() {
        return badGameDir;
    }

    public String getGameDir() {
        return gameDir.get();
    }

    public void setGameDir(String gameDir) {
        this.gameDir.set(gameDir);
    }

    public StringProperty gameDirProperty() {
        return gameDir;
    }

    public void saveSettingsSync() {
        TextUtils.writeText(nbl.nblPaths.settingsFile, Json.build(new SettingsJson(
                gameDir.get()
        )).toString(), StandardCharsets.UTF_8);
    }
    public void saveSettings(Runnable done) {
        SimpleTask<Boolean> task = saveSettings_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }

    public void loadSettings(Runnable done) {
        SimpleTask<SettingsJson> task = loadSettings_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }
    private SimpleTask<SettingsJson> loadSettings_get() {
        return new SimpleTask<SettingsJson>() {

            @Override public void uiInit() {
                updateMessage("load settings");
            }

            @Override protected SettingsJson call() throws Exception {
                if(!Files.exists(nbl.nblPaths.settingsFile)) return null;
                String content = TextUtils.readText(nbl.nblPaths.settingsFile, StandardCharsets.UTF_8);
                return Json.GSON.fromJson(Json.parse(content), SettingsJson.class);
            }

            @Override public void uiDone(@Nullable SettingsJson value) {
                if(value != null) {
                    if(value.getGameDir() != null) {
                        gameDir.setValue(value.getGameDir());
                    }
                }
            }
        };
    }

    private SimpleTask<Boolean> saveSettings_get() {
        return new SimpleTask<Boolean>() {

            private String gameDir;

            @Override public void uiInit() {
                updateMessage("save settings");
                gameDir = SettingsController.this.gameDir.getValue();
            }

            @Override protected Boolean call() throws Exception {
                TextUtils.writeText(nbl.nblPaths.settingsFile, Json.build(new SettingsJson(
                        gameDir
                )).toString(), StandardCharsets.UTF_8);
                return true;
            }

        };
    }

    public boolean validateGameDir() {
        boolean exists = Files.exists(Paths.get(getGameDir()));
        setBadGameDir(!exists);
        return exists;
    }

    public boolean validate() {
        if(!validateGameDir()) return false;
        return true;
    }

    public Path getGameDirPath() {
        return Paths.get(getGameDir());
    }

}
