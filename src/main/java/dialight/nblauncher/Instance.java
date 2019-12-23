package dialight.nblauncher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dialight.minecraft.DateMc;
import dialight.misc.HttpRequest;
import dialight.misc.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Instance {

    private final String id;
    private final String type;
    private final String url;
    private final Date time;

    public Instance(String id, String type, String url, Date time) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public Date getTime() {
        return time;
    }

    public static List<Instance> parseJson(String json) {
        List<Instance> list = new ArrayList<>();
        JsonArray versions = Json.parse(json).getAsJsonObject().getAsJsonArray("versions");
        for (JsonElement el : versions) {
            JsonObject version = el.getAsJsonObject();
            String id = version.get("id").getAsString();
            String type = version.get("type").getAsString();
            String url = version.get("url").getAsString();
            Date time = DateMc.deserializeToDate(version.get("time").getAsString());
            Date releaseTime = DateMc.deserializeToDate(version.get("releaseTime").getAsString());
            list.add(new Instance(id, type, url, time));
        }
        return list;
    }

}
