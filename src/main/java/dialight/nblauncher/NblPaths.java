package dialight.nblauncher;

import java.nio.file.Path;

public class NblPaths {

    public final Path homeDir;
    public final Path versionsDir;
    public final Path versionsFile;
    public final Path accountsFile;
    public final Path settingsFile;
    public final Path etagFile;
    public final Path guiPersistence;
    public final Path exceptionsFile;

    public NblPaths(Path homeDir) {
        this.homeDir = homeDir;
        this.versionsDir = this.homeDir.resolve("versions");
        this.versionsFile = this.homeDir.resolve("versions.json");
        this.accountsFile = this.homeDir.resolve("accounts.json");
        this.settingsFile = this.homeDir.resolve("settings.json");
        this.etagFile = this.homeDir.resolve("etag.json");
        this.guiPersistence = this.homeDir.resolve("gui_persistence.json");
        this.exceptionsFile = this.homeDir.resolve("exceptions.log");
    }

}
