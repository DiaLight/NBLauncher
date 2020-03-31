package dialight.minecraft.json.versions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import dialight.misc.Json;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Version {

    @SerializedName("id")
    private final String id;

    @SerializedName("type")
    private final String type;

    @SerializedName("url")
    private final String url;

    @SerializedName("sha1")
    private final String sha1;

    @SerializedName("time")
    private final Date time;

    @SerializedName("releaseTime")
    private Date releaseTime;

    @SerializedName("modifiers")
    private List<DisplayEntry> modifiers;

    public Version(String id, String type, String url, String sha1, Date time) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.sha1 = sha1;
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

    public String getSha1() {
        return sha1;
    }

    public Date getTime() {
        return time;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    @NotNull public List<DisplayEntry> getModifiers() {
        if(modifiers == null) return Collections.emptyList();
        return modifiers;
    }

    public static List<Version> parseJson(String json) {
        List<Version> list = new ArrayList<>();
        JsonArray versions = Json.parse(json).getAsJsonObject().getAsJsonArray("versions");
        for (JsonElement el : versions) {
            Version version = Json.GSON.fromJson(el, Version.class);
            if(version.id == null) continue;
//            JsonObject version = el.getAsJsonObject();
//            if(!version.has("id")) continue;
//            String id = version.get("id").getAsString();
//            String type = version.get("type").getAsString();
//            String sha1 = version.has("sha1") ? version.get("sha1").getAsString() : null;
//            String url = version.get("url").getAsString();
//            Date time = DateAdapter.deserializeToDate(version.get("time").getAsString());
//            Date releaseTime = DateAdapter.deserializeToDate(version.get("releaseTime").getAsString());
//            list.add(new Instance(id, type, url, sha1, time));
            list.add(version);
        }
        return list;
    }

}
