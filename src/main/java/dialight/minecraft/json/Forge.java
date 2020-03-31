package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Forge {

    @SerializedName("modlist")
    private List<ForgeMod> modlist;

    @SerializedName("absolutePrefix")
    private boolean absolutePrefix;

    @SerializedName("newModApi")
    private boolean newModApi;

    public Forge() {
        this.modlist = new ArrayList<>();
        this.absolutePrefix = false;
        this.newModApi = false;
    }
    public Forge(List<ForgeMod> modlist, boolean absolutePrefix, boolean newModApi) {
        this.modlist = modlist;
        this.absolutePrefix = absolutePrefix;
        this.newModApi = newModApi;
    }

    @NotNull public List<ForgeMod> getModlist() {
        if(modlist == null) return Collections.emptyList();
        return modlist;
    }

    public boolean isAbsolutePrefix() {
        return absolutePrefix;
    }

    public boolean isNewModApi() {
        return newModApi;
    }

    public void inherit(Forge parent) {
        if(parent.modlist != null) modlist.addAll(parent.modlist);
        if(!this.absolutePrefix) absolutePrefix = parent.absolutePrefix;
        if(!this.newModApi) newModApi = parent.newModApi;
    }

}
