package dialight.minecraft.json.libs;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import dialight.minecraft.json.Arguments;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.regex.Pattern;

public class Os {

    public static final Name CURRENT_OS;
    public static final String SYSTEM_VERSION = System.getProperty("os.version");
    public static final String SYSTEM_ARCHITECTURE;

    static {
        String name = System.getProperty("os.name").toLowerCase(Locale.US);
        if (name.contains("win")) {
            CURRENT_OS = Name.WINDOWS;
        } else if (name.contains("mac")) {
            CURRENT_OS = Name.OSX;
        } else if (name.contains("solaris") || name.contains("linux") || name.contains("unix") || name.contains("sunos")) {
            CURRENT_OS = Name.LINUX;
        } else {
            CURRENT_OS = null;
        }

        String arch = System.getProperty("sun.arch.data.model");
        if (arch == null) arch = System.getProperty("os.arch");
        SYSTEM_ARCHITECTURE = arch;
    }

    @SerializedName("name")
    private final Name name;

    @SerializedName("version")
    private final String version;

    @SerializedName("arch")
    private final String arch;


    public Os(Name name) {
        this(name, null);
    }
    public Os(Name name, String versio) {
        this(name, versio, null);
    }
    public Os(Name name, String version, String arch) {
        this.name = name;
        this.version = version;
        this.arch = arch;
    }

    public Name getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getArch() {
        return arch;
    }

    public boolean allow() {
        if (name != null && name != CURRENT_OS) return false;
        if (version != null && !Pattern.compile(version).matcher(SYSTEM_VERSION).matches()) return false;
        if (arch != null && !Pattern.compile(arch).matcher(SYSTEM_ARCHITECTURE).matches()) return false;

        return true;
    }

    @Override public String toString() {
        return "Os{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", arch='" + arch + '\'' +
                '}';
    }

    public enum Name {

        /**
         * Microsoft Windows.
         */
        WINDOWS("windows"),
        /**
         * Linux and Unix like OS, including Solaris.
         */
        LINUX("linux"),
        /**
         * Mac OS X.
         */
        OSX("osx");

        private final String name;

        Name(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static class Serialize implements JsonDeserializer<Name> {

            public static final Serialize INSTANCE = new Serialize();

            @Override public Name deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Name.valueOf(json.getAsString().toUpperCase());
            }
        }

    }

}
