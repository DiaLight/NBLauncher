package dialight.minecraft.json.versions;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VersionMapEntry {

    @SerializedName("id")
    private String id;

    @SerializedName("versions")
    private List<DisplayEntry> versions;

    public String getId() {
        return id;
    }

    public List<DisplayEntry> getVersions() {
        return versions;
    }

}
