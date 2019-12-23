package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class AssetsCfg {

    @SerializedName("objects")
    private final Map<String, Asset> objects;

    public AssetsCfg(Map<String, Asset> objects) {
        this.objects = objects;
    }

    public Map<String, Asset> getObjects() {
        return objects;
    }

}
