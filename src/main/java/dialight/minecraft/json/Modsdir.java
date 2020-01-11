package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Modsdir {

    @SerializedName("modlist")
    private List<Library> modlist;

    public Modsdir(List<Library> modlist) {
        this.modlist = modlist;
    }

    @NotNull public List<Library> getModlist() {
        if(modlist == null) return Collections.emptyList();
        return modlist;
    }

    public void inherit(Modsdir parent) {
        if(this.modlist == null) this.modlist = parent.modlist;
        else if(parent.modlist != null) modlist.addAll(parent.modlist);
    }

}
