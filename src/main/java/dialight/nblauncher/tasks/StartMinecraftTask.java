package dialight.nblauncher.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.JavaVersion;
import dialight.minecraft.LibName;
import dialight.minecraft.MinecraftAccount;
import dialight.minecraft.ResolvedNative;
import dialight.minecraft.json.*;
import dialight.minecraft.json.args.ArgPart;
import dialight.minecraft.json.args.RuleArg;
import dialight.minecraft.json.args.StringArg;
import dialight.minecraft.json.libs.Os;
import dialight.minecraft.json.libs.Rule;
import dialight.misc.*;
import dialight.minecraft.json.versions.Version;
import dialight.nblauncher.NBLauncher;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StartMinecraftTask extends SimpleTask<Boolean> {

    private final Path gameDir;
    private final String id;
    private final Map<String, Version> versions;
    private final MinecraftAccount profile;
    private final List<String> modifiers;
    private final NBLauncher nbl;

    private final Path versionDir;
    private final Path nativesDir;

    public StartMinecraftTask(Path gameDir, String id, Map<String, Version> versions, MinecraftAccount profile, List<String> modifiers, NBLauncher nbl) {
        this.gameDir = gameDir;
        this.id = id;
        this.versions = versions;
        this.profile = profile;
        this.modifiers = modifiers;
        this.nbl = nbl;

        this.versionDir = nbl.nblPaths.versionsDir.resolve(id);
        this.nativesDir = versionDir.resolve("natives");
    }

    @Override public void uiInit() {
        updateMessage("start minecraft");
    }

    private VersionCfg resolveConfig(String id) throws IOException {
        updateMessage(id + ".json");
        Path configFile = versionDir.resolve(id + ".json");
        JsonObject json = null;
        Version version = versions.get(id);
        if(version == null) throw new IllegalStateException("Instance " + id + " is not found");
        if(!Files.exists(configFile)) {
            json = Json.parse(HttpRequest.get(version.getUrl()).text).getAsJsonObject();
            Files.createDirectories(configFile.getParent());
            TextUtils.writeText(configFile, json.toString(), StandardCharsets.UTF_8);
        } else {
            if (version.getSha1() != null && !version.getSha1().equals(DigestUtils.sha1(configFile))) {
                json = Json.parse(HttpRequest.get(version.getUrl()).text).getAsJsonObject();
                TextUtils.writeText(configFile, json.toString(), StandardCharsets.UTF_8);
            } else {
                json = Json.parse(TextUtils.readText(configFile, StandardCharsets.UTF_8)).getAsJsonObject();
            }
//                json = Json.parse(TextUtils.readText(configFile)).getAsJsonObject();
        }
        if(json == null) throw new IllegalStateException("can't resolve instance config");
        return Json.GSON.fromJson(json, VersionCfg.class);
    }
    private void applyInherits_impl(String id, List<String> modifiers, VersionCfg out, Set<String> applied) throws Exception {
        if(applied.contains(id)) return;
        applied.add(id);
        VersionCfg current = resolveConfig(id);
        for (String inh : current.collectInherits()) {
            applyInherits_impl(inh, modifiers, out, applied);
        }
        out.inherit(current);
        List<ConfigBase> modifiersList = current.getModifiers();
        for (ConfigBase mod : modifiersList) {
            if(!modifiers.contains(mod.getId())) continue;
            out.inherit(mod);
            for (String modInh : mod.collectInherits()) {
                applyInherits_impl(modInh, modifiers, out, applied);
            }
        }
    }
    private VersionCfg applyInherits(String id, List<String> modifiers) throws Exception {
        VersionCfg out = new VersionCfg();
        Set<String> applied = new HashSet<>();
        applyInherits_impl(id, modifiers, out, applied);
        return out;
    }

    private void download(String url, Path jar, String name) throws IOException {
        BiConsumer<Long, Long> progress = (cur, total) -> {
            updateMessage(name + " (" + cur + "/" + total + " bytes)");
            this.updateProgress(cur, total);
        };
        try {
            updateMessage(name + " (try connect)");
            for (int i = 2; i <= 3; i++) {
                if (FileUtils.download(url, jar, progress)) return;
                updateMessage(name + " (try connect " + i + ")");
            }
        } finally {
            this.updateProgress(-1, 0);
        }
        throw new IOException("failed to download " + url);
    }
    private void resolve(String url, int size, String sha1, Path jar, String name) throws IOException {
        updateMessage(name);
        boolean update = false;
        if(!Files.exists(jar)) {
            // download
            Path parentFile = jar.getParent();
            if(!Files.exists(parentFile)) Files.createDirectories(parentFile);
            update = true;
        } else {
            if(sha1 != null) {
                String jarSha1 = DigestUtils.sha1(jar);
                if (!jarSha1.equals(sha1)) {
                    System.out.println("hash failed " + jar + " " + jarSha1 + " != " + sha1);
                    update = true;
                }
            } else {
                update = true;
            }
            if(size != 0) {
                long jarSize = Files.size(jar);
                if(jarSize != size) {
                    System.out.println("size failed " + jar + " " + jarSize + " != " + size);
                    update = true;
                }
            }
        }
        if(update) {
            // update
            download(url, jar, name);
            if(sha1 == null) {
                System.out.println("artifact " + name + " should have size: " + Files.size(jar) + "  sha1: " + DigestUtils.sha1(jar));
            } else {
                System.out.println("downloaded " + name + " size: " + Files.size(jar) + "  sha1: " + DigestUtils.sha1(jar));
            }
        }
    }
    private void resolveArtifact(Artifact artifact, Path jar, String name) throws IOException {
        resolve(artifact.getUrl(), artifact.getSize(), artifact.getSha1(), jar, name);
    }

    private void resolveLibs(VersionCfg versionCfg, List<String> classpath, List<ResolvedNative> natives) throws IOException {
        List<String> atTheEnd = new ArrayList<>();
        for (Library library : versionCfg.getLibraries()) {
            if(library.getName() == null) continue;
            if (!library.appliesToCurrentEnvironment((feature, enabled) -> {
                throw new IllegalStateException(feature + " " + enabled);
            })) continue;
            String url = library.resolveUrl();
            if(url != null) {
                Path jar = nbl.mcPaths.libsDir.resolve(library.resolvePath());
                resolve(url, library.resolveSize(), library.resolveSha1(), jar, library.getName());
                if(!library.isDownloadOnly()) {
                    if(library.isLoadLast()) {
                        atTheEnd.add(jar.toAbsolutePath().toString());
                    } else {
                        classpath.add(jar.toAbsolutePath().toString());
                    }
                }
            }
            String nativeLib = library.getNatives().get(Os.CURRENT_OS.getName());
            if(nativeLib != null) {
                nativeLib = nativeLib.replace("${arch}", Os.SYSTEM_ARCHITECTURE);
                ArtifactWithPath nativeArtifact = library.getDownloads().getClassifiers().get(nativeLib);
                if(nativeArtifact == null) throw new NullPointerException(library.getName() + " " + nativeLib);
                String path = nativeArtifact.getPath();
                Objects.requireNonNull(path);
                Path nativeJar = nbl.mcPaths.libsDir.resolve(path);
                resolveArtifact(nativeArtifact, nativeJar, library.getName() + " " + nativeLib);
                natives.add(new ResolvedNative(nativeJar, library));
            }
        }
        classpath.addAll(atTheEnd);
    }

    private void resolveForge(Forge forge) throws IOException {
        for (Library library : forge.getModlist()) {
            String path = library.resolvePath();
            Path jar = nbl.mcPaths.libsDir.resolve(path);
            resolve(library.resolveUrl(), library.resolveSize(), library.resolveSha1(), jar, library.getName());
        }
    }
    private void resolveModsdir(Modsdir modsdir) throws IOException {
        for (Library library : modsdir.getModlist()) {
            String path = library.resolvePath();
            Path jar = nbl.mcPaths.libsDir.resolve(path);
            resolve(library.resolveUrl(), library.resolveSize(), library.resolveSha1(), jar, library.getName());
        }
    }
    private Path resolveGameJar(VersionCfg versionCfg) throws IOException {
        Artifact client = versionCfg.getDownloads().get("client");
        Objects.requireNonNull(client);

        String name = versionCfg.getName();
        Path jar;
        if(name == null) {
            name = id + ".jar";
            jar = versionDir.resolve(name);
            if(Files.exists(jar)) return jar;
        } else {
            LibName libName = LibName.parse(name);
            String path = libName.buildPath();
            jar = nbl.mcPaths.libsDir.resolve(path);
            if(Files.exists(jar)) return jar;
            Path orig = nbl.mcPaths.versionsDir
                    .resolve(libName.getVersion())
                    .resolve(libName.getVersion() + ".jar");
            if(Files.exists(orig)) {
                String jarSha1 = DigestUtils.sha1(orig);
                if (jarSha1.equals(client.getSha1())) {
                    Files.createDirectories(jar.getParent());
                    Files.copy(orig, jar);
                    return jar;
                }
            }
        }
        resolveArtifact(client, jar, name);
        return jar;
    }
    private void unpackNatives(List<ResolvedNative> natives) {
        try {
            for (ResolvedNative nativeLib : natives) {
                BiConsumer<Long, Long> progress = (cur, total) -> {
                    updateMessage("unpack " + nativeLib.getLibrary().getName() + " (" + cur + "/" + total + " bytes)");
                    this.updateProgress(cur, total);
                };
                ZipFile zip = new ZipFile(nativeLib.getFile().toString());
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if(entry.isDirectory()) continue;
                    String entryName = entry.getName();
                    if(entryName.endsWith(".sha1") || entryName.endsWith(".git")) continue;
                    if(entryName.endsWith("MANIFEST.MF")) continue;
                    if(entryName.endsWith("module-info.class")) continue;
                    if(entryName.endsWith("INDEX.LIST")) continue;
//                        if(entryName.contains("/") || entryName.contains("\\")) {
//                            continue;
//                        }
                    Path outFile = nativesDir.resolve(entryName);
                    try(InputStream is = zip.getInputStream(entry)) {
                        FileUtils.write(is, entry.getSize(), outFile, progress);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AssetsCfg resolveAssetsCfg(VersionCfg versionCfg) throws IOException {
        Files.createDirectories(nbl.mcPaths.assetsIndexesDir);
        Path assetsIndexFile = nbl.mcPaths.assetsIndexesDir.resolve(versionCfg.getAssetIndex().getId() + ".json");
        resolveArtifact(versionCfg.getAssetIndex(), assetsIndexFile, "assets index " + versionCfg.getAssetIndex().getId());
        JsonElement json = Json.parse(TextUtils.readText(assetsIndexFile, StandardCharsets.UTF_8)).getAsJsonObject();
        if(json == null) throw new IllegalStateException("can't resolve instance config");
        return Json.GSON.fromJson(json, AssetsCfg.class);
    }

    private void resolveAssets(AssetsCfg assetsCfg) throws IOException {
        Files.createDirectories(nbl.mcPaths.assetsObjectsDir);
        for (Map.Entry<String, Asset> entry : assetsCfg.getObjects().entrySet()) {
            String name = entry.getKey();
            Asset asset = entry.getValue();
            String subHash = asset.getHash().substring(0, 2);
            String url = "https://resources.download.minecraft.net/" + subHash + "/" + asset.getHash();
            Path hashDir = nbl.mcPaths.assetsObjectsDir.resolve(subHash);
            if(!Files.exists(hashDir)) Files.createDirectories(hashDir);
            Path assetFile = hashDir.resolve(asset.getHash());
            resolve(url, asset.getSize(), asset.getHash(), assetFile, "asset " + name);
        }
    }

    private Path resolveLogging(VersionCfg versionCfg) throws IOException {
        ArtifactWithId artifact = versionCfg.getLogging().getClient().getFile();
        Path logCfg = nbl.mcPaths.logConfigsDir.resolve(artifact.getId());
        resolve(artifact.getUrl(), artifact.getSize(), artifact.getSha1(), logCfg, artifact.getId());
        return logCfg;
    }

    @Override protected Boolean call() throws Exception {
        if(profile == null) throw new IllegalStateException("profile is not selected");
        if(id == null) throw new IllegalStateException("version is not selected");
        if(!Files.exists(versionDir)) Files.createDirectories(versionDir);
        if(Files.exists(nativesDir)) FileUtils.deleteDirectory(nativesDir);
        if(!Files.exists(nativesDir)) Files.createDirectories(nativesDir);


        VersionCfg versionCfg = applyInherits(id, modifiers);


        List<String> classpath = new ArrayList<>();
        List<ResolvedNative> natives = new ArrayList<>();
        resolveLibs(versionCfg, classpath, natives);
        Path gameJar = resolveGameJar(versionCfg);
        classpath.add(gameJar.toAbsolutePath().toString());
        unpackNatives(natives);


        // resolve assets
        AssetsCfg assetsCfg = resolveAssetsCfg(versionCfg);
        resolveAssets(assetsCfg);

        // resolve logging
        Path logsCfg = resolveLogging(versionCfg);

        if(versionCfg.getForge() != null) resolveForge(versionCfg.getForge());
        if(versionCfg.getModsdir() != null) resolveModsdir(versionCfg.getModsdir());

        updateMessage("building args");
        // minecraft args
        Map<String, String> cfg = new HashMap<>();
        String classpathStr = String.join(File.pathSeparator, classpath);
        cfg.put("${classpath}", classpathStr);
        cfg.put("${game_jar}", gameJar.toAbsolutePath().toString());
        cfg.put("${natives_directory}", nativesDir.toAbsolutePath().toString());
        cfg.put("${game_assets}", nbl.mcPaths.assetsDir.toAbsolutePath().toString());
        cfg.put("${assets_root}", nbl.mcPaths.assetsDir.toAbsolutePath().toString());
        cfg.put("${auth_player_name}", profile.getName());
        cfg.put("${auth_uuid}", UuidAdapter.encode(profile.getUuid()));
        cfg.put("${auth_access_token}", profile.getAccessToken());
        cfg.put("${user_type}", "mojang");
        cfg.put("${user_properties}", "{}");
        cfg.put("${game_directory}", gameDir.toAbsolutePath().toString());
        cfg.put("${game_libraries}", nbl.mcPaths.libsDir.toAbsolutePath().toString());
        cfg.put("${assets_index_name}", versionCfg.getAssetIndex().getId());
        cfg.put("${version_name}", id);
        cfg.put("${version_type}", versionCfg.getType());
        cfg.put("${path}", logsCfg.toAbsolutePath().toString());

        Manifest manifest = FileUtils.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String launcher_name = attributes.getValue("Implementation-Title");
        cfg.put("${launcher_name}", launcher_name != null ? launcher_name : "test");
        String launcher_version = attributes.getValue("Implementation-Version");
        cfg.put("${launcher_version}", launcher_version != null ? launcher_version : "0.1");


        Map<String, Boolean> features = new HashMap<>();
        features.put("is_demo_user", false);
        features.put("has_custom_resolution", false);

        Arguments arguments = versionCfg.getArguments();
        if(arguments.isLegacy()) {
            List<ArgPart> jvm = new ArrayList<>();
            jvm.add(new RuleArg(Collections.singletonList(new Rule(Rule.Action.ALLOW, new Os(Os.Name.WINDOWS))), Collections.singletonList("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")));
            jvm.add(new RuleArg(Collections.singletonList(new Rule(Rule.Action.ALLOW, new Os(Os.Name.WINDOWS, "^10\\."))), Arrays.asList("-Dos.name=Windows 10", "-Dos.version=10.0")));
            jvm.add(new StringArg("-Djava.library.path=${natives_directory}"));
            jvm.add(new StringArg("-Dminecraft.launcher.brand=${launcher_name}"));
            jvm.add(new StringArg("-Dminecraft.launcher.version=${launcher_version}"));
            jvm.add(new StringArg("-cp"));
            jvm.add(new StringArg("${classpath}"));
            jvm.addAll(arguments.getJvm());
            arguments = new Arguments(arguments.getGame(), jvm, false);
        }
        List<ArgPart> jvm = arguments.getJvm();
        jvm.add(new StringArg("-Dminecraft.client.jar=${game_jar}"));

//            if (Os.CURRENT_OS == Os.Name.OSX) {
//                res.add("-Xdock:name=Minecraft " + version.getId());
//                res.add("-Xdock:icon=" + repository.getAssetObject(version.getId(), version.getAssetIndex().getId(), "icons/minecraft.icns").getAbsolutePath());
//            }

//                if (Os.CURRENT_OS != Os.Name.WINDOWS) {
//                    args.add("-Duser.home=" + options.getGameDir().getParent());
//                }

        // Force using G1GC with its settings
        if (JavaVersion.getMajorJavaVersion() >= 7) {
            jvm.add(new StringArg("-XX:+UnlockExperimentalVMOptions"));
            jvm.add(new StringArg("-XX:+UseG1GC"));
            jvm.add(new StringArg("-XX:G1NewSizePercent=20"));
            jvm.add(new StringArg("-XX:G1ReservePercent=20"));
            jvm.add(new StringArg("-XX:MaxGCPauseMillis=50"));
            jvm.add(new StringArg("-XX:G1HeapRegionSize=32M"));
        }

//                if (options.getMetaspace() != null && options.getMetaspace() > 0) {
//                    if (options.getJava().getParsedVersion() < JavaVersion.JAVA_8) {
//                        args.add("-XX:PermSize= " + options.getMetaspace() + "m");
//                    } else {
//                        args.add("-XX:MetaspaceSize=" + options.getMetaspace() + "m");
//                    }
//                }

//            jvm.add(new StringArg("-XX:-UseAdaptiveSizePolicy"));
//            jvm.add(new StringArg("-XX:-OmitStackTraceInFastThrow"));
//            jvm.add(new StringArg("-Xmn128m"));

        // As 32-bit JVM allocate 320KB for stack by default rather than 64-bit version allocating 1MB,
        // causing Minecraft 1.13 crashed accounting for java.lang.StackOverflowError.
//            if (options.getJava().getPlatform() == Platform.BIT_32) {
//                jvm.add(new StringArg("-Xss1M"));
//            }

//                if (options.getMaxMemory() != null && options.getMaxMemory() > 0) {
//                    args.add("-Xmx" + options.getMaxMemory() + "m");
//                }
//
//                if (options.getMinMemory() != null && options.getMinMemory() > 0) {
//                    args.add("-Xms" + options.getMinMemory() + "m");
//                }

//            jvm.add(new StringArg("-Dfml.ignoreInvalidMinecraftCertificates=true"));
//            jvm.add(new StringArg("-Dfml.ignorePatchDiscrepancies=true"));

        jvm.add(new StringArg(versionCfg.getLogging().getClient().getArgument()));


        List<String> args = new ArrayList<>();

        args.add("java");
        args.add("-Xss1M");

        args.addAll(arguments.bakeJvmArgs(key -> {
            String value = cfg.get(key);
            if(value == null) throw new NullPointerException("unknown key " + key);
            return value;
        }, (feature, enabled) -> {
            Boolean isEnabled = features.get(feature);
            if(isEnabled == null) throw new NullPointerException("unknown feature " + feature);
            return isEnabled == enabled;
        }));

        args.add(versionCfg.getMainClass());

        args.addAll(arguments.bakeGameArgs(key -> {
            String value = cfg.get(key);
            if(value == null) throw new NullPointerException("unknown key " + key);
            return value;
        }, (feature, enabled) -> {
            Boolean isEnabled = features.get(feature);
            if(isEnabled == null) throw new NullPointerException("unknown feature " + feature);
            return isEnabled == enabled;
        }));

//        args.addAll(Arrays.asList("--server", server, "--port", port));
//        args.add("--fullscreen");
//        args.addAll(Arrays.asList("--proxyHost", proxyHost, "--proxyPort", proxyPort));
//        args.addAll(Arrays.asList("--proxyUser", proxyUser, "--proxyPass", proxyPass));


        List<Path> toDelete = new ArrayList<>();
        if (versionCfg.getForge() != null) {
            Path modsDir = gameDir.resolve("mods");
            Files.createDirectories(modsDir);
            List<String> names = new ArrayList<>();
            for (ForgeMod mod : versionCfg.getForge().getModlist()) {
                if(mod.useModsDir()) {
                    Path source = nbl.mcPaths.libsDir.resolve(mod.resolvePath());
                    Path target = modsDir.resolve("tmp_" + source.getFileName().toString());
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    toDelete.add(target);
                    System.out.println("mod: " + mod.getName());
                } else {
                    names.add(mod.getName());
                    System.out.println("mod(args): " + mod.getName());
                }
            }
            if(!names.isEmpty()) {
                if (versionCfg.getForge().isNewModApi()) {
                    args.addAll(Arrays.asList("--fml.mavenRoots", nbl.mcPaths.libsDir.toString()));
                    args.addAll(Arrays.asList("--fml.mods", String.join(",", names)));
                } else {
                    JsonObject modlist = new JsonObject();
                    String prefix = "";
                    if (versionCfg.getForge().isAbsolutePrefix()) {
                        prefix = "absolute:";
                    }
                    modlist.addProperty("repositoryRoot", prefix + nbl.mcPaths.libsDir.toString());
                    JsonArray modref = new JsonArray();
                    for (String name : names) {
                        modref.add(name);
                    }

                    modlist.add("modRef", modref);
                    String modlistJson = modlist.toString();

                    String modlistPath = "tempModList-" + DigestUtils.crc32(modlistJson.getBytes());
                    Path modlistFile = gameDir.resolve(modlistPath);
                    TextUtils.writeText(modlistFile, modlistJson, StandardCharsets.UTF_8);
                    args.addAll(Arrays.asList("--modListFile", modlistPath));
                    toDelete.add(modlistFile);
                }
            }
        }
        if (versionCfg.getModsdir() != null) {
            Path modsDir = gameDir.resolve("mods");
            Files.createDirectories(modsDir);
            for (Library mod : versionCfg.getModsdir().getModlist()) {
                Path source = nbl.mcPaths.libsDir.resolve(mod.resolvePath());
                Path target = modsDir.resolve("tmp_" + source.getFileName().toString());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                toDelete.add(target);
                System.out.println("mod: " + mod.getName());
            }
        }

        String last = "";
        for (String arg : args) {
            if(last.equals("-cp")) {
                String libspath = nbl.mcPaths.libsDir.toString();
                for (String lib : arg.split(File.pathSeparator)) {
                    if(!Files.exists(Paths.get(lib))) throw new IllegalStateException("file " + lib + " not exist");
                    if(lib.startsWith(libspath)) lib = lib.substring(libspath.length() + 1);
                    System.out.println("lib: " + lib);
                }
            } else {
                System.out.print(arg);
            }
            if(!arg.startsWith("--")) System.out.println();
            else System.out.print(" ");
            last = arg;
        }

        updateMessage("starting minecraft");
        Process process = new ProcessBuilder(args)
                .directory(gameDir.toFile())
                .start();
        try(ProcessIO io = new ProcessIO(process)) {
            while (io.waitForIO() || io.hasIO()) {
                String line;
                while((line = io.popStdout()) != null) {
                    System.out.println(line);
                }
                while((line = io.popStderr()) != null) {
                    System.out.println(line);
                }
            }
        } finally {
            for (Path file : toDelete) {
                Files.deleteIfExists(file);
            }
            FileUtils.deleteDirectory(nativesDir);
        }

        return true;
    }

}
