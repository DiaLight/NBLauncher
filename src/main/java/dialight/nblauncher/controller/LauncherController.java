package dialight.nblauncher.controller;

import dialight.minecraft.DirsMap;
import dialight.minecraft.MCVersion;
import dialight.minecraft.json.versions.Profile;
import dialight.minecraft.json.versions.Version;
import dialight.minecraft.json.versions.Versions;
import dialight.misc.*;
import dialight.mvc.Controller;
import dialight.mvc.MVCApplication;
import dialight.nblauncher.tasks.StartMinecraftTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import dialight.extensions.CollectionEx;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

public class LauncherController extends Controller {


    private final DirsMap dirsMap = new DirsMap(new File(System.getProperty("user.dir")));

    private final ObjectProperty<Map<String, Version>> versions = new SimpleObjectProperty<>(Collections.emptyMap());
    private final ObjectProperty<List<Profile>> profiles = new SimpleObjectProperty<>(Collections.emptyList());
    private final ObjectProperty<List<String>> modifiers = new SimpleObjectProperty<>(Collections.emptyList());

    private final SimpleObjectProperty<String> selectedVersion = new SimpleObjectProperty<>(null);

    private final Supplier<SimpleTask<Versions>> loadCachedVersions = () -> new SimpleTask<Versions>() {

        @Override public void uiInit() {
            updateMessage("load cached versions");
        }

        @Override protected Versions call() throws Exception {
            if(!dirsMap.versionsFile().exists()) return null;
            return Json.GSON.fromJson(Json.parse(TextUtils.readText(dirsMap.versionsFile())), Versions.class);
        }

        @Override public void uiDone(@Nullable Versions value) {
            if(value != null) {
                updateVersions(value.getVersions());
                updateProfiles(value.getProfiles());
            }
            if(versions.get().isEmpty()) {
                progress.scheduleTask(loadOnlineVersions.get());
            }
        }

    };

    private final Supplier<SimpleTask<Versions>> loadOnlineVersions = () -> new SimpleTask<Versions>() {

        @Override public void uiInit() {
            updateMessage("load online versions");
        }

        @Override protected Versions call() throws Exception {
//            String json = HttpRequest.read("https://launchermeta.mojang.com/mc/game/version_manifest.json");  // official
            String json = HttpRequest.read("https://clientshield.mrlegolas.ru/repo/versions.json");
            dirsMap.versionsFile().getParentFile().mkdirs();
            TextUtils.writeText(dirsMap.versionsFile(), json);
            return Json.GSON.fromJson(Json.parse(json), Versions.class);
        }

        @Override public void uiDone(@Nullable Versions value) {
            if(value != null) {
                updateVersions(value.getVersions());
                updateProfiles(value.getProfiles());
            }
        }

    };

    private ProgressController progress;
    private AccountsController profile;

    private final Supplier<SimpleTask<Boolean>> startMinecraft = () -> new StartMinecraftTask(
            selectedVersion.get(),
            versions.get(),
            profile.getSelectedAccount(),
            modifiers.get(),
            dirsMap
    );

    @Override protected void init(MVCApplication app) {
        progress = app.findController(ProgressController.class);
        profile = app.findController(AccountsController.class);
        progress.scheduleTask(loadCachedVersions.get());
    }

    private void updateVersions(List<Version> versions) {
        Map<String, Version> filtered = new HashMap<>();
        for (Version version : versions) {
            if(version.getId() == null) continue;
            filtered.put(version.getId(), version);
        }
        this.versions.setValue(filtered);
        if(selectedVersion.get() == null) {
            selectedVersion.set(CollectionEx.of(filtered.keySet()).firstOrNull());
        }
    }
    private void updateProfiles(List<Profile> profiles) {
        for (Profile profile : profiles) {
            String id = profile.getId();
            if(id == null) continue;
        }
        this.profiles.set(profiles);
    }

    public Version getVersion(String id) {
        return versions.get().get(id);
    }

    public void selectVersion(String version) {
        selectedVersion.setValue(version);
    }

    public SimpleObjectProperty<String> selectedVersionProperty() {
        return selectedVersion;
    }

    public void startMinecraft() {
        profile.validateSelected(isValid -> {
            if(!isValid) {
                profile.refreshSelected(succ ->  {
                    if(succ) {
                        profile.validateSelected(isValid2 -> {
                            if (!isValid2) {
                                System.out.println("failed to validate refreshed token");
                            } else {
                                progress.scheduleTask(startMinecraft.get());
                            }
                        });
                    } else {
                        System.out.println("failed to refresh token");
                    }
                });
            } else {
                progress.scheduleTask(startMinecraft.get());
            }
        });
    }

    public void loadOnlineVersions() {
        progress.scheduleTask(loadOnlineVersions.get());
    }

    public List<Profile> getProfiles() {
        return profiles.get();
    }

    public ObjectProperty<List<Profile>> profilesProperty() {
        return profiles;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers.setValue(modifiers);
    }

    @Nullable public Profile getProfile(String id) {
        for (Profile cur : profiles.get()) {
            if(cur.getId().equals(id)) return cur;
        }
        return null;
    }
}
