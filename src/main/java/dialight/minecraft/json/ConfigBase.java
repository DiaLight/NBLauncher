package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConfigBase {

    @SerializedName("id")
    private String id;

    @SerializedName("displayName")
    private String displayName;

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

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        if(displayName == null) return id;
        return displayName;
    }

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
        if(this.arguments == null) this.arguments = new Arguments(parent.getArguments().isLegacy());
        if(this.libraries == null) this.libraries = new ArrayList<>();
        if(this.forge == null) this.forge = new Forge();
        if(this.modsdir == null) this.modsdir = parent.modsdir;

        if(parent.id != null) this.id = parent.id;
        if(parent.displayName != null) this.displayName = parent.displayName;
        if(parent.arguments != null) arguments.inherit(parent.arguments);
        if(parent.libraries != null) libraries.addAll(0, parent.libraries);

        if(parent.forge != null) forge.inherit(parent.forge);
        if(parent.modsdir != null) modsdir.inherit(parent.modsdir);
    }

}
