package dialight.nblauncher;

import dialight.minecraft.MCPaths;

import java.nio.file.Path;

public class NBLauncher {

//    public final String versionsUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";  // official
    public final String versionsUrl = "https://clientshield.mrlegolas.ru/repo/versions.json";
    public final String githubUrl = "https://api.github.com/repos/DiaLight/NBLauncher/releases/latest";
    public final Path homeDir;
    public final MCPaths mcPaths;
    public final NblPaths nblPaths;

    public NBLauncher(Path homeDir) {
        this.homeDir = homeDir;
        this.mcPaths = new MCPaths(homeDir);
        this.nblPaths = new NblPaths(homeDir.resolve(".nblauncher"));
    }

}
