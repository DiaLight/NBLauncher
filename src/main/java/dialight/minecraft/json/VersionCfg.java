package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VersionCfg extends ConfigBase {

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
    private List<ConfigBase> modifiers;

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

    @NotNull public List<ConfigBase> getModifiers() {
        if(modifiers == null) return Collections.emptyList();
        return modifiers;
    }

    @Override public void inherit(ConfigBase _parent) {
        if(!(_parent instanceof VersionCfg)) {
            super.inherit(_parent);
            return;
        }
        VersionCfg parent = (VersionCfg) _parent;
        if(parent.name != null) this.name = parent.name;
        if(parent.mainClass != null) this.mainClass = parent.mainClass;
        if(parent.releaseTime != null) this.releaseTime = parent.releaseTime;
        if(parent.time != null) this.time = parent.time;
        if(parent.minimumLauncherVersion != null) this.minimumLauncherVersion = parent.minimumLauncherVersion;
        if(parent.type != null) this.type = parent.type;
        if(parent.assets != null) this.assets = parent.assets;
        if(parent.logging != null) this.logging = parent.logging;
        if(parent.assetIndex != null) this.assetIndex = parent.assetIndex;
        if(parent.downloads != null) {
            if(this.downloads == null) this.downloads = parent.downloads;
            else for (Map.Entry<String, Artifact> entry : parent.downloads.entrySet()) {
                downloads.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        super.inherit(parent);
    }
}
