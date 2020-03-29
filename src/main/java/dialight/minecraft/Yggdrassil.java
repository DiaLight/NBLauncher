package dialight.minecraft;

import com.google.gson.JsonObject;
import dialight.minecraft.json.UuidAdapter;
import dialight.misc.HttpRequest;
import dialight.misc.Json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Yggdrassil {


    public static MinecraftAccount refresh(MinecraftAccount profile) throws IOException {
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
        return new MinecraftAccount(profile.getName(), profile.getUuid(), clientToken, accessToken);
    }

    public static boolean validate(MinecraftAccount profile) throws IOException {
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

    public static boolean invalidate(MinecraftAccount profile) throws IOException {
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

    public static MinecraftAccount authenticate(String username, String password) throws IOException {
        String newClientToken = UuidAdapter.encode(UUID.randomUUID());

        Map<String, Object> request = new HashMap<>();
        Map<String, Object> agent = new HashMap<>();
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        request.put("username", username);
        request.put("password", password);
        request.put("clientToken", newClientToken);
        request.put("requestUser", true);
        String content = Json.build(request).toString();
        String result = HttpRequest.post("https://authserver.mojang.com/authenticate", content, "application/json");
        JsonObject answer = Json.parse(result).getAsJsonObject();
        if(answer.has("error")) {
            String error = answer.get("error").getAsString();
            String errorMessage = answer.get("errorMessage").getAsString();
            if(error.equals("ForbiddenOperationException")) {
                throw new ForbiddenOperationException(errorMessage);
            }
            throw new IllegalStateException(error + ": " + errorMessage);
        }
        String clientToken = answer.get("clientToken").getAsString();
        String accessToken = answer.get("accessToken").getAsString();
        JsonObject selectedProfile = answer.getAsJsonObject("selectedProfile");
        String name = selectedProfile.get("name").getAsString();
        UUID uuid = UuidAdapter.decode(selectedProfile.get("id").getAsString());
        return new MinecraftAccount(name, uuid, clientToken, accessToken);
    }

}
