package dialight.minecraft.json.versions;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Versions {

    @SerializedName("versions")
    private List<Version> versions;

    @SerializedName("gameTypes")
    private List<GameType> gameTypes;

    @SerializedName("minLauncherVersion")
    private String minLauncherVersion;

    public Versions() {}

    public Versions(List<Version> versions, List<GameType> gameTypes) {
        this.versions = versions;
        this.gameTypes = gameTypes;
    }

    @NotNull public List<Version> getVersions() {
        if(versions == null) return Collections.emptyList();
        return versions;
    }

    public List<GameType> getGameTypes() {
        if(gameTypes == null) return Collections.emptyList();
        return gameTypes;
    }

    public String getMinLauncherVersion() {
        return minLauncherVersion;
    }

}
