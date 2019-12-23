package dialight.nblauncher.controller;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dialight.extensions.CollectionEx;
import dialight.minecraft.MinecraftProfile;
import dialight.minecraft.json.UuidAdapter;
import dialight.misc.*;
import dialight.mvc.Controller;
import dialight.mvc.MVCApplication;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

public class ProfileController extends Controller {

    private final File profilesFile = new File("profiles.json");

    private final ObservableList<MinecraftProfile> profiles = FXCollections.observableArrayList();

    private final SimpleObjectProperty<MinecraftProfile> selectedProfile = new SimpleObjectProperty<>(null);

    private final SimpleObjectProperty<String> username = new SimpleObjectProperty<>(null);
    private final SimpleObjectProperty<String> password = new SimpleObjectProperty<>(null);

    private final Supplier<SimpleTask<Boolean>> saveProfiles = () -> new SimpleTask<Boolean>() {

        private List<MinecraftProfile> profiles;

        @Override public void uiInit() {
            updateMessage("save profiles");
            profiles = new ArrayList<>(ProfileController.this.profiles);
        }

        @Override protected Boolean call() throws Exception {
            TextUtils.writeText(profilesFile, Json.build(profiles).toString());
            return true;
        }

    };
    private final Supplier<SimpleTask<List<MinecraftProfile>>> loadProfiles = () -> new SimpleTask<List<MinecraftProfile>>() {

        @Override public void uiInit() {
            updateMessage("load profiles");
        }

        @Override protected List<MinecraftProfile> call() throws Exception {
            if(!profilesFile.exists()) return Collections.emptyList();
            List<MinecraftProfile> profiles = new ArrayList<>();
            String content = TextUtils.readText(profilesFile);
            for (JsonElement element : Json.parse(content).getAsJsonArray()) {
                JsonObject user = element.getAsJsonObject();
                MinecraftProfile profile = Json.GSON.fromJson(user, MinecraftProfile.class);
                profiles.add(profile);
            }
            return profiles;
        }

        @Override public void uiDone(@Nullable List<MinecraftProfile> value) {
            if(value != null) {
                profiles.addAll(value);
                if(selectedProfile.get() == null) {
                    selectedProfile.set(CollectionEx.of(value).firstOrNull());
                }
            }
        }
    };

    private final Supplier<SimpleTask<MinecraftProfile>> authenticate = () -> new SimpleTask<MinecraftProfile>() {
        private String username;
        private String password;
        private String clientToken = UuidAdapter.encode(UUID.randomUUID());

        @Override public void uiInit() {
            updateMessage("authenticate");
            username = ProfileController.this.username.get();
            password = ProfileController.this.password.get();
        }
        @Override protected MinecraftProfile call() throws Exception {
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> agent = new HashMap<>();
            agent.put("name", "Minecraft");
            agent.put("version", 1);
            request.put("agent", agent);
            request.put("username", username);
            request.put("password", password);
            request.put("clientToken", clientToken);
            request.put("requestUser", true);
            String content = Json.build(request).toString();
            String result = HttpRequest.post("https://authserver.mojang.com/authenticate", content, "application/json");
            JsonObject answer = Json.parse(result).getAsJsonObject();
            if(answer.has("error")) {
                String error = answer.get("error").getAsString();
                String errorMessage = answer.get("errorMessage").getAsString();
                throw new IllegalStateException(error + ": " + errorMessage);
            }
            String clientToken = answer.get("clientToken").getAsString();
            String accessToken = answer.get("accessToken").getAsString();
            JsonObject selectedProfile = answer.getAsJsonObject("selectedProfile");
            String name = selectedProfile.get("name").getAsString();
            UUID uuid = UuidAdapter.decode(selectedProfile.get("id").getAsString());
            return new MinecraftProfile(name, uuid, clientToken, accessToken);
        }

        @Override public void uiDone(@Nullable MinecraftProfile value) {
            if(value != null) {
                profiles.add(value);
                progress.scheduleTask(saveProfiles.get());
                if(selectedProfile.get() == null) {
                    selectedProfile.set(value);
                }
            }
        }
    };
    private final Supplier<SimpleTask<MinecraftProfile>> refresh = () -> new SimpleTask<MinecraftProfile>() {

        private MinecraftProfile profile;

        @Override public void uiInit() {
            updateMessage("refresh");
            profile = selectedProfile.get();
        }

        @Override protected MinecraftProfile call() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("clientToken", profile.getClientToken());
            request.put("accessToken", profile.getAccessToken());
            String content = Json.build(request).toString();
            String result = HttpRequest.post("https://authserver.mojang.com/refresh", content, "application/json");
            JsonObject answer = Json.parse(result).getAsJsonObject();
            if(answer.has("error")) {
                String error = answer.get("error").getAsString();
                String errorMessage = answer.get("errorMessage").getAsString();
                throw new IllegalStateException(error + ": " + errorMessage);
            }
            String clientToken = answer.get("clientToken").getAsString();
            String accessToken = answer.get("accessToken").getAsString();
            return new MinecraftProfile(profile.getName(), profile.getUuid(), clientToken, accessToken);
        }
    };
    private final Supplier<SimpleTask<Boolean>> validate = () -> new SimpleTask<Boolean>() {

        private MinecraftProfile profile;

        @Override public void uiInit() {
            updateMessage("validate");
            profile = selectedProfile.get();
        }

        @Override protected Boolean call() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("clientToken", profile.getClientToken());
            request.put("accessToken", profile.getAccessToken());
            String content = Json.build(request).toString();
            String result = HttpRequest.post("https://authserver.mojang.com/validate", content, "application/json");
            if(result.isEmpty()) return true;
            JsonObject answer = Json.parse(result).getAsJsonObject();
            if(answer.has("error")) {
                String error = answer.get("error").getAsString();
                if(error.equals("ForbiddenOperationException")) return false;
                String errorMessage = answer.get("errorMessage").getAsString();
                throw new IllegalStateException(error + ": " + errorMessage);
            }
            throw new IllegalStateException("unknown answer: " + result);
        }
    };
    private final Supplier<SimpleTask<Boolean>> invalidate = () -> new SimpleTask<Boolean>() {

        private MinecraftProfile profile;

        @Override public void uiInit() {
            updateMessage("invalidate");
            profile = selectedProfile.get();
        }

        @Override protected Boolean call() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("clientToken", profile.getClientToken());
            request.put("accessToken", profile.getAccessToken());
            String content = Json.build(request).toString();
            String result = HttpRequest.post("https://authserver.mojang.com/invalidate", content, "application/json");
            if(result.isEmpty()) return true;
            JsonObject answer = Json.parse(result).getAsJsonObject();
            if(answer.has("error")) {
                String error = answer.get("error").getAsString();
                if(error.equals("ForbiddenOperationException")) return false;
                String errorMessage = answer.get("errorMessage").getAsString();
                throw new IllegalStateException(error + ": " + errorMessage);
            }
            throw new IllegalStateException("unknown answer: " + result);
        }
    };
    private ProgressController progress;

    @Override protected void init(MVCApplication app) {
        progress = app.findController(ProgressController.class);
        progress.scheduleTask(loadProfiles.get());
    }

    public ObservableList<MinecraftProfile> getProfiles() {
        return profiles;
    }

    public MinecraftProfile getSelectedProfile() {
        return selectedProfile.get();
    }

    public SimpleObjectProperty<MinecraftProfile> selectedProfileProperty() {
        return selectedProfile;
    }

    public Property<String> usernameProperty() {
        return username;
    }
    public Property<String> passwordProperty() {
        return password;
    }

    public void authenticate() {
        progress.scheduleTask(authenticate.get());
    }

    public void delete(MinecraftProfile profile) {
        profiles.remove(profile);
        progress.scheduleTask(invalidate.get());
        progress.scheduleTask(saveProfiles.get());
    }

}
