package dialight.nblauncher.controller;

import dialight.minecraft.json.ETagEntry;
import dialight.minecraft.json.ETagJson;
import dialight.minecraft.json.versions.GameType;
import dialight.minecraft.json.versions.Version;
import dialight.minecraft.json.versions.Versions;
import dialight.misc.*;
import dialight.mvc.Controller;
import dialight.mvc.InitCtx;
import dialight.mvc.MVCApplication;
import dialight.nblauncher.NBLauncher;
import dialight.nblauncher.json.GuiPersistence;
import dialight.nblauncher.tasks.StartMinecraftTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class LauncherController extends Controller {

    private final ObjectProperty<Map<String, Version>> versions = new SimpleObjectProperty<>(Collections.emptyMap());
    private final ObjectProperty<List<GameType>> gameTypes = new SimpleObjectProperty<>(Collections.emptyList());
    private final ObjectProperty<List<String>> modifiers = new SimpleObjectProperty<>(Collections.emptyList());

    private final Map<String, ETagEntry> etag = new HashMap<>();
    private GuiPersistence guiPersistence = new GuiPersistence();

    private final NBLauncher nbl;

    private ProgressController progress;
    private AccountsController profile;

    public LauncherController(NBLauncher nbl) {
        this.nbl = nbl;
    }

    @Override protected void init(InitCtx ctx, MVCApplication app, Runnable done) {
        progress = app.findController(ProgressController.class);
        profile = app.findController(AccountsController.class);
        loadGuiPersistence(() -> {
            loadETagCache(() -> {
                loadCachedVersions(() -> {
                    ctx.fireInit(app, done);
                });
            });
        });
    }

    private void updateVersions(List<Version> versions) {
        Map<String, Version> filtered = new HashMap<>();
        for (Version version : versions) {
            if(version.getId() == null) continue;
            filtered.put(version.getId(), version);
        }
        this.versions.setValue(filtered);
    }
    private void updateGameTypes(List<GameType> gameTypes) {
        for (GameType gameType : gameTypes) {
            String id = gameType.getId();
            if(id == null) continue;
        }
        this.gameTypes.set(gameTypes);
    }

    public GuiPersistence getGuiPersistence() {
        return guiPersistence;
    }

    public Version getVersion(String id) {
        return versions.get().get(id);
    }

    public void startMinecraft(String version, Path gameDir) {
        Objects.requireNonNull(profile.getSelectedAccount());
        profile.validateSelected(isValid -> {
            if(!isValid) {
                Objects.requireNonNull(profile.getSelectedAccount());
                profile.refreshSelected(succ ->  {
                    if(succ) {
                        Objects.requireNonNull(profile.getSelectedAccount());
                        profile.validateSelected(isValid2 -> {
                            if (!isValid2) {
                                System.out.println("failed to validate refreshed token");
                            } else {
                                progress.scheduleTask(startMinecraft_get(version, gameDir));
                            }
                        });
                    } else {
                        System.out.println("failed to refresh token");
                    }
                });
            } else {
                progress.scheduleTask(startMinecraft_get(version, gameDir));
            }
        });
    }

    public List<GameType> getGameTypes() {
        return gameTypes.get();
    }

    public ObjectProperty<List<GameType>> gameTypesProperty() {
        return gameTypes;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers.setValue(modifiers);
    }

    @Nullable public GameType getGameType(String id) {
        for (GameType cur : gameTypes.get()) {
            if(cur.getId().equals(id)) return cur;
        }
        return null;
    }

    private void loadCachedVersions(Runnable done) {
        SimpleTask<Versions> task = loadCachedVersions_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }

    public void loadOnlineVersions(Runnable done) {
        SimpleTask<Versions> task = loadOnlineVersions_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }

    public void loadETagCache(Runnable done) {
        SimpleTask<?> task = loadETag_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }

    public void loadGuiPersistence(Runnable done) {
        SimpleTask<?> task = guiPersistence_get();
        task.setOnSucceeded(event -> {
            done.run();
        });
        progress.scheduleTask(task);
    }

    private SimpleTask<Versions> loadCachedVersions_get() {
        return new SimpleTask<Versions>() {

            @Override public void uiInit() {
                updateMessage("load cached versions");
            }

            @Override protected Versions call() throws Exception {
                if(!Files.exists(nbl.nblPaths.versionsFile)) return null;
                try {
                    return Json.GSON.fromJson(Json.parse(TextUtils.readText(nbl.nblPaths.versionsFile, StandardCharsets.UTF_8)), Versions.class);
                } catch(Exception e) {
                    return new Versions();
                }
            }

            @Override public void uiDone(@Nullable Versions value) {
                if(value != null) {
                    updateVersions(value.getVersions());
                    updateGameTypes(value.getGameTypes());
                }
            }

        };
    }

    private SimpleTask<Versions> loadOnlineVersions_get() {
        return new SimpleTask<Versions>() {

            ETagEntry eTagEntry;

            @Override public void uiInit() {
                updateMessage("load online versions");
                eTagEntry = etag.get(nbl.versionsUrl);
            }

            @Override protected Versions call() throws Exception {
                HttpRequest.Resource head = HttpRequest.head(nbl.versionsUrl);
                boolean download = false;
                if(!Files.exists(nbl.nblPaths.versionsFile)) {
                    download = true;
                } else if(eTagEntry == null || eTagEntry.getRemote().before(head.lastModified)) {
                    download = true;
                }
                if(download) {
                    try {
                        System.out.println("download new version");
                        HttpRequest.Resource json = HttpRequest.get(nbl.versionsUrl);
                        Files.createDirectories(nbl.nblPaths.versionsFile.getParent());
                        TextUtils.writeText(nbl.nblPaths.versionsFile, json.text, StandardCharsets.UTF_8);
                        eTagEntry = new ETagEntry(
                                nbl.versionsUrl, json.eTag, DigestUtils.sha1(json.text),
                                Files.getLastModifiedTime(nbl.nblPaths.versionsFile).toMillis(),
                                json.lastModified
                        );
                        return Json.GSON.fromJson(Json.parse(json.text), Versions.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override public void uiDone(@Nullable Versions value) {
                if(value != null) {
                    updateVersions(value.getVersions());
                    updateGameTypes(value.getGameTypes());
                    etag.put(eTagEntry.getUrl(), eTagEntry);
                }
            }

        };
    }

    private SimpleTask<Collection<ETagEntry>> loadETag_get() {
        return new SimpleTask<Collection<ETagEntry>>() {

            @Override public void uiInit() {
                updateMessage("load etag cache");
            }

            @Override protected Collection<ETagEntry> call() throws Exception {
                if(!Files.exists(nbl.nblPaths.etagFile)) return Collections.emptyList();
                return Json.GSON.fromJson(Json.parse(TextUtils.readText(nbl.nblPaths.etagFile, StandardCharsets.UTF_8)), ETagJson.class).getETag();
            }

            @Override public void uiDone(@Nullable Collection<ETagEntry> value) {
                if(value != null) {
                    for (ETagEntry entry : value) {
                        ETagEntry oldVal = etag.get(entry.getUrl());
                        if(oldVal == null || oldVal.getRemote().before(entry.getRemote())) {
                            etag.put(entry.getUrl(), entry);
                        }
                    }
                }
            }

        };
    }

    private SimpleTask<GuiPersistence> guiPersistence_get() {
        return new SimpleTask<GuiPersistence>() {

            @Override public void uiInit() {
                updateMessage("load gui persistence");
            }

            @Override protected GuiPersistence call() throws Exception {
                if(!Files.exists(nbl.nblPaths.guiPersistence)) return null;
                return Json.GSON.fromJson(Json.parse(TextUtils.readText(nbl.nblPaths.guiPersistence, StandardCharsets.UTF_8)), GuiPersistence.class);
            }

            @Override public void uiDone(@Nullable GuiPersistence value) {
                if(value != null) {
                    guiPersistence = value;
                }
            }

        };
    }

    private SimpleTask<Boolean> startMinecraft_get(String version, Path gameDir) {
        return new StartMinecraftTask(
                gameDir,
                version,
                versions.get(),
                profile.getSelectedAccount(),
                modifiers.get(),
                nbl
        );
    }

    public void save() {
        TextUtils.writeText(nbl.nblPaths.guiPersistence, Json.build(guiPersistence).toString(), StandardCharsets.UTF_8);

        ETagJson json = new ETagJson(new ArrayList<>(etag.values()));
        TextUtils.writeText(nbl.nblPaths.etagFile, Json.build(json).toString(), StandardCharsets.UTF_8);
    }
}
