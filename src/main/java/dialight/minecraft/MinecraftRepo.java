package dialight.minecraft;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class MinecraftRepo {

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

    public static Path getMinecraftPath() {
        switch (CURRENT_OS) {
            case WINDOWS:
                return Paths.get(System.getenv("APPDATA"), ".minecraft");
            case LINUX:
                return Paths.get(System.getProperty("user.home"), ".minecraft");
            case OSX:
                return Paths.get(System.getProperty("user.home"), "Library/Application Support/minecraft");
            default:
                throw new RuntimeException("Can't resolve minecraft path");
        }
    }

}
