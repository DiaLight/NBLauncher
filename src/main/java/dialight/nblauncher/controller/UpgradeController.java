package dialight.nblauncher.controller;

import dialight.minecraft.MinecraftAccount;
import dialight.misc.*;
import dialight.mvc.Controller;
import dialight.mvc.InitCtx;
import dialight.mvc.MVCApplication;
import dialight.nblauncher.NBLauncher;
import dialight.nblauncher.json.GithubAsset;
import dialight.nblauncher.json.GithubRelease;
import dialight.nblauncher.json.SettingsJson;
import dialight.nblauncher.view.upgrade.AssetCell;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class UpgradeController extends Controller {

    private final NBLauncher nbl;

    private final ObservableList<AssetCell> assets = FXCollections.observableArrayList();

    private ProgressController progress;
    private SceneController scene;
    private boolean latest = true;

    public UpgradeController(NBLauncher nbl) {
        this.nbl = nbl;
    }

    public boolean isLatest() {
        return latest;
    }

    public ObservableList<AssetCell> getAssets() {
        return assets;
    }

    @Override protected void init(InitCtx ctx, MVCApplication app, Runnable done) {
        progress = app.findController(ProgressController.class);
        scene = app.findController(SceneController.class);
        loadGithubRelease(() -> {
            ctx.fireInit(app, done);
        });
    }

    public void loadGithubRelease(Runnable done) {
        SimpleTask<GithubRelease> task = loadGithubRelease_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }
    private SimpleTask<GithubRelease> loadGithubRelease_get() {
        return new SimpleTask<GithubRelease>() {

            @Override public void uiInit() {
                updateMessage("load github release info");
            }

            @Override protected GithubRelease call() throws Exception {
                HttpRequest.Resource json = HttpRequest.get(nbl.githubUrl);
                return Json.GSON.fromJson(Json.parse(json.text), GithubRelease.class);
            }

            @Override public void uiDone(@Nullable GithubRelease value) {
                if(value != null && !value.isPrerelease()) {
                    Manifest manifest = FileUtils.getManifest();
                    Attributes attributes = manifest.getMainAttributes();
                    String launcher_version = attributes.getValue("Implementation-Version");
                    if(launcher_version == null) launcher_version = "0.0";
                    String curTagName = "v" + launcher_version;
                    latest = curTagName.equals(value.getTag_name());
                    assets.clear();
                    for (GithubAsset asset : value.getAssets()) {
                        AssetCell assetCell = new AssetCell(asset.getName(), asset.getSize(), asset.getBrowser_download_url(), scene.getApp());
                        assets.add(assetCell);
                    }
                }
            }
        };
    }

}
