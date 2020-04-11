package dialight.nblauncher.json;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.MinecraftAccount;

import java.util.List;
import java.util.UUID;

public class SettingsJson {

    @SerializedName("gameDir")
    private String gameDir;

    public SettingsJson(String gameDir) {
        this.gameDir = gameDir;
    }

    public String getGameDir() {
        return gameDir;
    }

}
