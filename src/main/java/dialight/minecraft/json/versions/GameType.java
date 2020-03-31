package dialight.minecraft.json.versions;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.MCVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameType {

    @SerializedName("id")
    private String id;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("versionMapping")
    private List<VersionMapEntry> versionMapping;

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        if(displayName == null) return id;
        return displayName;
    }

    public Map<MCVersion, List<DisplayEntry>> collectVersionMap() {
        Map<MCVersion, List<DisplayEntry>> map = new HashMap<>();
        for (VersionMapEntry entry : versionMapping) {
            if(entry.getId() == null) continue;
            if(entry.getVersions() == null || entry.getVersions().isEmpty()) continue;
            MCVersion mcv = MCVersion.parse(entry.getId());
            map.put(mcv, entry.getVersions());
        }
        return map;
    }

}
