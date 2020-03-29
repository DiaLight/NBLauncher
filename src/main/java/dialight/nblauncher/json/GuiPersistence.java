package dialight.nblauncher.json;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiPersistence {

    @SerializedName("gameTypeId")
    private String gameTypeId;

    @SerializedName("mcVersionMap")
    private Map<String, String> mcVersionMap;

    @SerializedName("versionMap")
    private Map<String, String> versionMap;

    @SerializedName("modifiersMap")
    private Map<String, List<String>> modifiersMap;

    public String getGameTypeId() {
        return gameTypeId;
    }
    public void setGameTypeId(String id) {
        gameTypeId = id;
    }

    public Map<String, String> getMcVersionMap() {
        if(mcVersionMap == null) return Collections.emptyMap();
        return mcVersionMap;
    }

    public void putMcVersion(String id, String mcVersion) {
        if(mcVersionMap == null) mcVersionMap = new HashMap<>();
        mcVersionMap.put(id, mcVersion);
    }

    public Map<String, String> getVersionMap() {
        if(versionMap == null) return Collections.emptyMap();
        return versionMap;
    }
    public void putVersion(String typeVersion, String version) {
        if(versionMap == null) versionMap = new HashMap<>();
        versionMap.put(typeVersion, version);
    }

    public Map<String, List<String>> getModifiersMap() {
        if(modifiersMap == null) return Collections.emptyMap();
        return modifiersMap;
    }
    public void putModifiers(String version, List<String> modifiers) {
        if(modifiersMap == null) modifiersMap = new HashMap<>();
        if(modifiers.isEmpty()) {
            modifiersMap.remove(version);
        } else {
            modifiersMap.put(version, modifiers);
        }
    }

}
