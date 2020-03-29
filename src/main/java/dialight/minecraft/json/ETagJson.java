package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.MinecraftAccount;

import java.util.Collections;
import java.util.List;

public class ETagJson {

    @SerializedName("eTag")
    private List<ETagEntry> eTag;

    public ETagJson(List<ETagEntry> eTag) {
        this.eTag = eTag;
    }

    public List<ETagEntry> getETag() {
        if(eTag == null) return Collections.emptyList();
        return eTag;
    }

}
