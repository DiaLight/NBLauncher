package dialight.minecraft.json.versions;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VersionMapEntry {

    @SerializedName("id")
    private String id;

    @SerializedName("versions")
    private List<String> versions;

    public String getId() {
        return id;
    }

    public List<String> getVersions() {
        return versions;
    }

}
