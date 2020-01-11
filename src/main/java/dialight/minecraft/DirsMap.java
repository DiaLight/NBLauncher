package dialight.minecraft;

import java.io.File;

public class DirsMap {


    private final File homeDir;

    private final File versionsDir;
    private final File versionsFile;

    private final File libsDir;
    private final File assetsDir;

    private final File assetsIndexesDir;
    private final File assetsObjectsDir;
    private final File logConfigsDir;

    public DirsMap(File homeDir) {
        this.homeDir = homeDir;
        assetsDir = new File(this.homeDir, "assets");
        libsDir = new File(this.homeDir, "libraries");
        versionsDir = new File(this.homeDir, "versions");
        versionsFile = new File(versionsDir, "versions.json");

        assetsIndexesDir = new File(assetsDir, "indexes");
        assetsObjectsDir = new File(assetsDir, "objects");
        logConfigsDir = new File(assetsDir, "log_configs");
    }

    public File home() {
        return homeDir;
    }

    public File instancesDir() {
        return versionsDir;
    }

    public File versionsFile() {
        return versionsFile;
    }

    public File libsDir() {
        return libsDir;
    }

    public File assetsDir() {
        return assetsDir;
    }

    public File assetsIndexesDir() {
        return assetsIndexesDir;
    }

    public File assetsObjectsDir() {
        return assetsObjectsDir;
    }

    public File logConfigsDir() {
        return logConfigsDir;
    }
}
