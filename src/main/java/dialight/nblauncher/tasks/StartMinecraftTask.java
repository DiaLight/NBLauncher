package dialight.nblauncher.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.JavaVersion;
import dialight.minecraft.DirsMap;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StartMinecraftTask extends SimpleTask<Boolean> {

    private final String id;
    private final Map<String, Version> versions;
    private final MinecraftAccount profile;
    private final List<String> modifiers;
    private final DirsMap dirsMap;

    private final File instanceDir;
    private final File nativesDir;

    public StartMinecraftTask(String id, Map<String, Version> versions, MinecraftAccount profile, List<String> modifiers, DirsMap dirsMap) {
        this.id = id;
        this.versions = versions;
        this.profile = profile;
        this.modifiers = modifiers;
        this.dirsMap = dirsMap;

        instanceDir = new File(dirsMap.instancesDir(), id);
        nativesDir = new File(instanceDir, "natives");
    }

    @Override public void uiInit() {
        updateMessage("start minecraft");
    }

    private VersionCfg resolveConfig(String id) throws IOException {
        updateMessage(id + ".json");
        File configFile = new File(dirsMap.instancesDir(), id + "/" + id + ".json");
        JsonObject json = null;
        Version version = versions.get(id);
        if(version == null) throw new IllegalStateException("Instance " + id + " is not found");
        if(!configFile.exists()) {
            json = Json.parse(HttpRequest.read(version.getUrl())).getAsJsonObject();
            configFile.getParentFile().mkdirs();
            TextUtils.writeText(configFile, json.toString());
        } else {
            if (version.getSha1() != null && !version.getSha1().equals(DigestUtils.sha1(configFile))) {
                json = Json.parse(HttpRequest.read(version.getUrl())).getAsJsonObject();
                TextUtils.writeText(configFile, json.toString());
            } else {
                json = Json.parse(TextUtils.readText(configFile)).getAsJsonObject();
            }
//                json = Json.parse(TextUtils.readText(configFile)).getAsJsonObject();
        }
        if(json == null) throw new IllegalStateException("can't resolve instance config");
        return Json.GSON.fromJson(json, VersionCfg.class);
    }
    private VersionCfg applyInherits(String id, List<String> modifiers) throws Exception {
        VersionCfg out = new VersionCfg();
        List<String> read = new ArrayList<>();
        List<String> write = new ArrayList<>();
        Set<String> applied = new HashSet<>();
        read.add(id);
        while(!read.isEmpty()) {
            for (String inherit : read) {
                if(applied.contains(inherit)) continue;
                applied.add(inherit);
                VersionCfg current = resolveConfig(inherit);
                Map<String, ConfigBase> modifiersMap = current.getModifiers();
                for (String modifier : modifiers) {
                    ConfigBase mod = modifiersMap.get(modifier);
                    if(mod == null) continue;
                    out.inherit(mod);
                    write.addAll(mod.collectInherits());
                }
                out.inherit(current);
                write.addAll(current.collectInherits());
            }
            read.clear();
            List<String> tmp = read;
            read = write;
            write = tmp;
        }
        return out;
    }

    private void download(String url, File jar, String name) throws IOException {
        BiConsumer<Long, Long> progress = (cur, total) -> {
            updateMessage(name + " (" + cur + "/" + total + " bytes)");
            this.updateProgress(cur, total);
        };
//                Timeline timeline = new Timeline(
//                        new KeyFrame(Duration.ZERO, new KeyValue(seconds, 0)),
//                        new KeyFrame(Duration.minutes(1), e-> {}, new KeyValue(seconds, 60))
//                );
//                timeline.setCycleCount(Animation.INDEFINITE);
//                timeline.play();
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
    private void resolve(String url, int size, String sha1, File jar, String name) throws IOException {
        updateMessage(name);
        boolean update = false;
        if(!jar.exists()) {
            // download
            File parentFile = jar.getParentFile();
            if(!parentFile.exists()) parentFile.mkdirs();
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
                long jarSize = jar.length();
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
                System.out.println("artifact " + name + " should have size: " + jar.length() + "  sha1: " + DigestUtils.sha1(jar));
            } else {
                System.out.println("downloaded " + name + " size: " + jar.length() + "  sha1: " + DigestUtils.sha1(jar));
            }
        }
    }
    private void resolveArtifact(Artifact artifact, File jar, String name) throws IOException {
        resolve(artifact.getUrl(), artifact.getSize(), artifact.getSha1(), jar, name);
    }

    private void resolveLibs(VersionCfg versionCfg, List<String> classpath, List<ResolvedNative> natives) throws IOException {
        List<String> atTheEnd = new ArrayList<>();
        for (Library library : versionCfg.getLibraries()) {
            if(library.getName() == null) continue;
            if(library.appliesToCurrentEnvironment((feature, enabled) -> {
                throw new IllegalStateException(feature + " " + enabled);
            })) {
                String url = library.resolveUrl();
                if(url != null) {
                    File jar = new File(dirsMap.libsDir(), library.resolvePath());
                    resolve(url, library.resolveSize(), library.resolveSha1(), jar, library.getName());
                    if(!library.isDownloadOnly()) {
                        if(library.isLoadLast()) {
                            atTheEnd.add(jar.getAbsolutePath());
                        } else {
                            classpath.add(jar.getAbsolutePath());
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
                    File nativeJar = new File(dirsMap.libsDir(), path);
                    resolveArtifact(nativeArtifact, nativeJar, library.getName() + " " + nativeLib);
                    natives.add(new ResolvedNative(nativeJar, library));
                }
            }
        }
        classpath.addAll(atTheEnd);
    }

    private void resolveForge(Forge forge) throws IOException {
        for (Library library : forge.getModlist()) {
            String path = library.resolvePath();
            File jar = new File(dirsMap.libsDir(), path);
            resolve(library.resolveUrl(), library.resolveSize(), library.resolveSha1(), jar, library.getName());
        }
    }
    private void resolveModsdir(Modsdir modsdir) throws IOException {
        for (Library library : modsdir.getModlist()) {
            String path = library.resolvePath();
            File jar = new File(dirsMap.libsDir(), path);
            resolve(library.resolveUrl(), library.resolveSize(), library.resolveSha1(), jar, library.getName());
        }
    }
    private File resolveGameJar(VersionCfg versionCfg) throws IOException {
        Artifact client = versionCfg.getDownloads().get("client");

        String name = versionCfg.getName();
        File jar;
        if(name == null) {
            name = id + ".jar";
            jar = new File(instanceDir, name);
        } else {
            String path = LibName.parse(name).buildPath();
            jar = new File(dirsMap.libsDir(), path);
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
                ZipFile zip = new ZipFile(nativeLib.getFile());
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
                    File outFile = new File(nativesDir, entryName);
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
        dirsMap.assetsIndexesDir().mkdirs();
        File assetsIndexFile = new File(dirsMap.assetsIndexesDir(), versionCfg.getAssetIndex().getId() + ".json");
        resolveArtifact(versionCfg.getAssetIndex(), assetsIndexFile, "assets index " + versionCfg.getAssetIndex().getId());
        JsonElement json = Json.parse(TextUtils.readText(assetsIndexFile)).getAsJsonObject();
        if(json == null) throw new IllegalStateException("can't resolve instance config");
        return Json.GSON.fromJson(json, AssetsCfg.class);
    }

    private void resolveAssets(AssetsCfg assetsCfg) throws IOException {
        dirsMap.assetsObjectsDir().mkdirs();
        for (Map.Entry<String, Asset> entry : assetsCfg.getObjects().entrySet()) {
            String name = entry.getKey();
            Asset asset = entry.getValue();
            String subHash = asset.getHash().substring(0, 2);
            String url = "https://resources.download.minecraft.net/" + subHash + "/" + asset.getHash();
            File hashDir = new File(dirsMap.assetsObjectsDir(), subHash);
            if(!hashDir.exists()) hashDir.mkdir();
            File assetFile = new File(hashDir, asset.getHash());
            resolve(url, asset.getSize(), asset.getHash(), assetFile, "asset " + name);
        }
    }

    private File resolveLogging(VersionCfg versionCfg) throws IOException {
        LoggingClient client = versionCfg.getLogging().getClient();
        File logCfg = new File(dirsMap.logConfigsDir(), client.getFile().getId());
        resolve(client.getFile().getUrl(), client.getFile().getSize(), client.getFile().getSha1(), logCfg, client.getFile().getId());
        return logCfg;
    }

    @Override protected Boolean call() throws Exception {
        if(profile == null) throw new IllegalStateException("profile is not selected");
        if(id == null) throw new IllegalStateException("version is not selected");
        if(!instanceDir.exists()) instanceDir.mkdirs();
        FileUtils.deleteDirectory(nativesDir);
        if(!nativesDir.exists()) nativesDir.mkdirs();


        VersionCfg versionCfg = applyInherits(id, modifiers);


        List<String> classpath = new ArrayList<>();
        List<ResolvedNative> natives = new ArrayList<>();
        resolveLibs(versionCfg, classpath, natives);
        File gameJar = resolveGameJar(versionCfg);
        classpath.add(gameJar.getAbsolutePath());
        unpackNatives(natives);


        // resolve assets
        AssetsCfg assetsCfg = resolveAssetsCfg(versionCfg);
        resolveAssets(assetsCfg);

        // resolve logging
        File logsCfg = resolveLogging(versionCfg);

        if(versionCfg.getForge() != null) resolveForge(versionCfg.getForge());
        if(versionCfg.getModsdir() != null) resolveModsdir(versionCfg.getModsdir());

        updateMessage("building args");
        // minecraft args
        Map<String, String> cfg = new HashMap<>();
        String classpathStr = String.join(File.pathSeparator, classpath);
        cfg.put("${classpath}", classpathStr);
        cfg.put("${game_jar}", gameJar.getAbsolutePath());
        cfg.put("${natives_directory}", nativesDir.getAbsolutePath());
        cfg.put("${game_assets}", dirsMap.assetsDir().getAbsolutePath());
        cfg.put("${assets_root}", dirsMap.assetsDir().getAbsolutePath());
        cfg.put("${auth_player_name}", profile.getName());
        cfg.put("${auth_uuid}", UuidAdapter.encode(profile.getUuid()));
        cfg.put("${auth_access_token}", profile.getAccessToken());
        cfg.put("${user_type}", "mojang");
        cfg.put("${user_properties}", "{}");
        cfg.put("${game_directory}", dirsMap.home().getAbsolutePath());
        cfg.put("${assets_index_name}", versionCfg.getAssetIndex().getId());
        cfg.put("${version_name}", id);
        cfg.put("${version_type}", versionCfg.getType());
        cfg.put("${path}", logsCfg.getAbsolutePath());

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
        if(arguments.getJvm().isEmpty()) {
            List<ArgPart> jvm = new ArrayList<>();
            jvm.add(new RuleArg(Collections.singletonList(new Rule(Rule.Action.ALLOW, new Os(Os.Name.WINDOWS))), Collections.singletonList("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")));
            jvm.add(new RuleArg(Collections.singletonList(new Rule(Rule.Action.ALLOW, new Os(Os.Name.WINDOWS, "^10\\."))), Arrays.asList("-Dos.name=Windows 10", "-Dos.version=10.0")));
            jvm.add(new StringArg("-Djava.library.path=${natives_directory}"));
            jvm.add(new StringArg("-Dminecraft.launcher.brand=${launcher_name}"));
            jvm.add(new StringArg("-Dminecraft.launcher.version=${launcher_version}"));
            jvm.add(new StringArg("-cp"));
            jvm.add(new StringArg("${classpath}"));
            arguments = new Arguments(arguments.getGame(), jvm, arguments.isComplete());
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



        String last = "";
        for (String arg : args) {
            if(last.equals("-cp")) {
                String libspath = dirsMap.libsDir().getPath();
                for (String lib : arg.split(":")) {
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

        List<File> toDelete = new ArrayList<>();
        if (versionCfg.getForge() != null) {
            File modsDir = new File(dirsMap.home(), "mods");
            modsDir.mkdirs();
            List<String> names = new ArrayList<>();
            for (ForgeMod mod : versionCfg.getForge().getModlist()) {
                if(mod.useModsDir()) {
                    File source = new File(dirsMap.libsDir(), mod.resolvePath());
                    File target = new File(modsDir, "tmp_" + source.getName());
                    Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    toDelete.add(target);
                    System.out.println("mod: " + mod.getName());
                } else {
                    names.add(mod.getName());
                    System.out.println("mod(args): " + mod.getName());
                }
            }
            if(!names.isEmpty()) {
                if (versionCfg.getForge().isNewModApi()) {
                    args.addAll(Arrays.asList("--fml.mavenRoots", dirsMap.libsDir().getPath()));
                    args.addAll(Arrays.asList("--fml.mods", String.join(",", names)));
                } else {
                    JsonObject modlist = new JsonObject();
                    String prefix = "";
                    if (versionCfg.getForge().isAbsolutePrefix()) {
                        prefix = "absolute:";
                    }
                    modlist.addProperty("repositoryRoot", prefix + dirsMap.libsDir().getPath());
                    JsonArray modref = new JsonArray();
                    for (String name : names) {
                        modref.add(name);
                    }

                    modlist.add("modRef", modref);
                    String modlistJson = modlist.toString();

                    String modlistPath = "tempModList-" + DigestUtils.crc32(modlistJson.getBytes());
                    File modlistFile = new File(dirsMap.home(), modlistPath);
                    TextUtils.writeText(modlistFile, modlistJson);
                    args.addAll(Arrays.asList("--modListFile", modlistPath));
                    toDelete.add(modlistFile);
                }
            }
        }
        if (versionCfg.getModsdir() != null) {
            File modsDir = new File(dirsMap.home(), "mods");
            modsDir.mkdirs();
            for (Library mod : versionCfg.getModsdir().getModlist()) {
                File source = new File(dirsMap.libsDir(), mod.resolvePath());
                File target = new File(modsDir, "tmp_" + source.getName());
                Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                toDelete.add(target);
                System.out.println("mod: " + mod.getName());
            }
        }

        updateMessage("starting minecraft");
        Process process = new ProcessBuilder(args)
                .inheritIO()
                .start();
        process.waitFor();

        for (File file : toDelete) {
            file.delete();
        }
        FileUtils.deleteDirectory(nativesDir);

        return true;
    }

}
