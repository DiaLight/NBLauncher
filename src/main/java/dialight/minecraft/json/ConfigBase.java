package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConfigBase {

    @SerializedName("inheritsFrom")
    private String inheritsFrom;

    @SerializedName("inheritsFromAll")
    private List<String> inheritsFromAll;

    @SerializedName(value = "arguments", alternate = { "minecraftArguments" })
    private Arguments arguments;

    @SerializedName("libraries")
    private List<Library> libraries;

    @SerializedName("forge")
    private Forge forge;

    @SerializedName("modsdir")
    private Modsdir modsdir;

    public List<String> collectInherits() {
        List<String> inherit = new ArrayList<>();
        if(inheritsFrom != null) inherit.add(inheritsFrom);
        if(inheritsFromAll != null) inherit.addAll(inheritsFromAll);
        return inherit;
    }

    public Arguments getArguments() {
        return arguments;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    @Nullable public Forge getForge() {
        return forge;
    }

    @Nullable public Modsdir getModsdir() {
        return modsdir;
    }

    public void inherit(ConfigBase parent) {
        if(this.arguments == null) this.arguments = parent.arguments;
        else if(parent.arguments != null) arguments.inherit(parent.arguments);
        if(this.libraries == null) this.libraries = parent.libraries;
        else if(parent.libraries != null) libraries.addAll(parent.libraries);

        if(this.forge == null) this.forge = parent.forge;
        else if(parent.forge != null) forge.inherit(parent.forge);
        if(this.modsdir == null) this.modsdir = parent.modsdir;
        else if(parent.modsdir != null) modsdir.inherit(parent.modsdir);
    }

}
