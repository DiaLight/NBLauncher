package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class InstanceCfg {

    @SerializedName("id")
    private final String id;

    @SerializedName("mainClass")
    private final String mainClass;

    @SerializedName("releaseTime")
    private final Date releaseTime;

    @SerializedName("time")
    private final Date time;

    @SerializedName("minimumLauncherVersion")
    private final int minimumLauncherVersion;

    @SerializedName("type")
    private final String type;

    @SerializedName("assets")
    private final String assets;

    @SerializedName(value = "arguments", alternate = { "minecraftArguments" })
    private final Arguments arguments;

    @SerializedName("logging")
    private final Logging logging;

    @SerializedName("assetIndex")
    private final ArtifactWithId assetIndex;

    @SerializedName("libraries")
    private final List<Library> libraries;

    @SerializedName("downloads")
    private final Map<String, Artifact> downloads;


    public InstanceCfg(String id, String mainClass, Date releaseTime, Date time, int minimumLauncherVersion, String type, String assets, Arguments arguments, Logging logging, ArtifactWithId assetIndex, List<Library> libraries, Map<String, Artifact> downloads) {
        this.id = id;
        this.mainClass = mainClass;
        this.releaseTime = releaseTime;
        this.time = time;
        this.minimumLauncherVersion = minimumLauncherVersion;
        this.type = type;
        this.assets = assets;
        this.arguments = arguments;
        this.logging = logging;
        this.assetIndex = assetIndex;
        this.libraries = libraries;
        this.downloads = downloads;
    }

    public String getId() {
        return id;
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

    public Arguments getArguments() {
        return arguments;
    }

    public Logging getLogging() {
        return logging;
    }

    public ArtifactWithId getAssetIndex() {
        return assetIndex;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public Map<String, Artifact> getDownloads() {
        return downloads;
    }

}
