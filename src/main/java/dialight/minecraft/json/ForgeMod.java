package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

public class ForgeMod extends Library {

    @SerializedName("useModsDir")
    private boolean useModsDir = false;

    public boolean useModsDir() {
        return useModsDir;
    }

}
