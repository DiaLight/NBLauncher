package dialight.minecraft.json.versions;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.MCVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile {

    @SerializedName("id")
    private String id;

    @SerializedName("versionMapping")
    private List<VersionMapEntry> versionMapping;

    public String getId() {
        return id;
    }

    public Map<MCVersion, List<String>> collectVersionMap() {
        Map<MCVersion, List<String>> map = new HashMap<>();
        for (VersionMapEntry entry : versionMapping) {
            if(entry.getId() == null) continue;
            if(entry.getVersions() == null || entry.getVersions().isEmpty()) continue;
            MCVersion mcv = MCVersion.parse(entry.getId());
            map.put(mcv, entry.getVersions());
        }
        return map;
    }

}
