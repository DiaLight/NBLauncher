package dialight.minecraft;

import java.nio.file.Path;

public class MCPaths {

    public final Path versionsDir;

    public final Path libsDir;
    public final Path assetsDir;

    public final Path assetsIndexesDir;
    public final Path assetsObjectsDir;
    public final Path logConfigsDir;

    public MCPaths(Path homeDir) {
        assetsDir = homeDir.resolve("assets");
        libsDir = homeDir.resolve("libraries");
        versionsDir = homeDir.resolve("versions");

        assetsIndexesDir = assetsDir.resolve("indexes");
        assetsObjectsDir = assetsDir.resolve("objects");
        logConfigsDir = assetsDir.resolve("log_configs");
    }

}
