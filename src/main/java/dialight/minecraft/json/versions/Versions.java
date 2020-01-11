package dialight.minecraft.json.versions;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Versions {

    @SerializedName("versions")
    private List<Version> versions;

    @SerializedName("profiles")
    private List<Profile> profiles;

    public Versions() {}

    public Versions(List<Version> versions, List<Profile> profiles) {
        this.versions = versions;
        this.profiles = profiles;
    }

    @NotNull public List<Version> getVersions() {
        if(versions == null) return Collections.emptyList();
        return versions;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

}
