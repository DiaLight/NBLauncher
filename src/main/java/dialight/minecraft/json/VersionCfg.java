package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VersionCfg extends ConfigBase {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("mainClass")
    private String mainClass;

    @SerializedName("releaseTime")
    private Date releaseTime;

    @SerializedName("time")
    private Date time;

    @SerializedName("minimumLauncherVersion")
    private Integer minimumLauncherVersion;

    @SerializedName("type")
    private String type;

    @SerializedName("assets")
    private String assets;

    @SerializedName("logging")
    private Logging logging;

    @SerializedName("assetIndex")
    private ArtifactWithId assetIndex;

    @SerializedName("downloads")
    private Map<String, Artifact> downloads;

    @SerializedName("modifiers")
    private Map<String, ConfigBase> modifiers;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMainClass() {
        return mainClass;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public Date getTime() {
        return time;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }

    public String getType() {
        return type;
    }

    public String getAssets() {
        return assets;
    }

    public Logging getLogging() {
        return logging;
    }

    public ArtifactWithId getAssetIndex() {
        return assetIndex;
    }

    public Map<String, Artifact> getDownloads() {
        return downloads;
    }

    @NotNull public Map<String, ConfigBase> getModifiers() {
        if(modifiers == null) return Collections.emptyMap();
        return modifiers;
    }

    @Override public void inherit(ConfigBase _parent) {
        if(!(_parent instanceof VersionCfg)) {
            super.inherit(_parent);
            return;
        }
        VersionCfg parent = (VersionCfg) _parent;
        if(this.id == null) this.id = parent.id;
        if(this.name == null) this.name = parent.name;
        if(this.mainClass == null) this.mainClass = parent.mainClass;
        if(this.releaseTime == null) this.releaseTime = parent.releaseTime;
        if(this.time == null) this.time = parent.time;
        if(this.minimumLauncherVersion == null) this.minimumLauncherVersion = parent.minimumLauncherVersion;
        if(this.type == null) this.type = parent.type;
        if(this.assets == null) this.assets = parent.assets;
        if(this.logging == null) this.logging = parent.logging;
        if(this.assetIndex == null) this.assetIndex = parent.assetIndex;
        if(parent.downloads != null) {
            if(this.downloads == null) this.downloads = parent.downloads;
            else for (Map.Entry<String, Artifact> entry : parent.downloads.entrySet()) {
                downloads.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        super.inherit(parent);
    }
}
