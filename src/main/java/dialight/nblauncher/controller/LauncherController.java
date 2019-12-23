package dialight.nblauncher.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.JavaVersion;
import dialight.minecraft.MinecraftProfile;
import dialight.minecraft.ResolvedNative;
import dialight.minecraft.json.*;
import dialight.minecraft.json.args.ArgPart;
import dialight.minecraft.json.args.RuleArg;
import dialight.minecraft.json.args.StringArg;
import dialight.minecraft.json.libs.Os;
import dialight.minecraft.json.libs.Rule;
import dialight.misc.*;
import dialight.mvc.Controller;
import dialight.mvc.MVCApplication;
import dialight.nblauncher.Instance;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import dialight.extensions.CollectionEx;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LauncherController extends Controller {

    private final File homeDir = new File(System.getProperty("user.dir"));

    private final File instancesDir = new File(homeDir, "instances");
    private final File instancesFile = new File(instancesDir, "instances.json");

    private final File libsDir = new File(homeDir, "libs");

    private final File assetsDir = new File(homeDir, "assets");
    private final File assetsIndexesDir = new File(assetsDir, "indexes");
    private final File assetsObjectsDir = new File(assetsDir, "objects");
    private final File logConfigsDir = new File(assetsDir, "log_configs");

    private final ObservableList<Instance> instances = FXCollections.observableArrayList();
    private final SimpleObjectProperty<String> instanceType = new SimpleObjectProperty<String>("release");

    private final SimpleObjectProperty<Instance> selectedInstance = new SimpleObjectProperty<>(null);

    private final Supplier<SimpleTask<List<Instance>>> loadCachedInstances = () -> new SimpleTask<List<Instance>>() {

        @Override public void uiInit() {
            updateMessage("load cached instances");
        }

        @Override protected List<Instance> call() throws Exception {
            if(!instancesFile.exists()) return Collections.emptyList();
            return Instance.parseJson(TextUtils.readText(instancesFile));
        }

        @Override public void uiDone(@Nullable List<Instance> value) {
            if(value != null) updateInstances(value);
            if(instances.isEmpty()) {
                progress.scheduleTask(loadOnlineInstances.get());
            }
        }

    };

    private final Supplier<SimpleTask<List<Instance>>> loadOnlineInstances = () -> new SimpleTask<List<Instance>>() {

        @Override public void uiInit() {
            updateMessage("load online instances");
        }

        @Override protected List<Instance> call() throws Exception {
            String json = HttpRequest.read("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            File parent = instancesFile.getParentFile();
            if(!parent.exists()) parent.mkdirs();
            TextUtils.writeText(instancesFile, json);
            return Instance.parseJson(json);
        }

        @Override public void uiDone(@Nullable List<Instance> value) {
            if(value != null) updateInstances(value);
        }

    };

    private final Supplier<SimpleTask<Boolean>> startMinecraft = () -> new SimpleTask<Boolean>() {

        private MinecraftProfile profile;
        private Instance instance;
        private File instanceDir;
        private File nativesDir;

        @Override public void uiInit() {
            updateMessage("start minecraft");
            instance = selectedInstance.get();
            profile = LauncherController.this.profile.getSelectedProfile();
            instanceDir = new File(instancesDir, instance.getId());
            nativesDir = new File(instanceDir, "natives");
        }

        private InstanceCfg resolveConfig() throws IOException {
            updateMessage(instance.getId() + ".json");
            File configFile = new File(instanceDir, instance.getId() + ".json");
            JsonObject json = null;
            if(!configFile.exists()) {
                json = Json.parse(HttpRequest.read(instance.getUrl())).getAsJsonObject();
                TextUtils.writeText(configFile, json.toString());
            } else {
                json = Json.parse(TextUtils.readText(configFile)).getAsJsonObject();
            }
            if(json == null) throw new IllegalStateException("can't resolve instance config");
            return Json.GSON.fromJson(json, InstanceCfg.class);
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
        private void resolve(String url, String sha1, File jar, String name) throws IOException {
            updateMessage(name);
            if(!jar.exists()) {
                // download
                File parentFile = jar.getParentFile();
                if(!parentFile.exists()) parentFile.mkdirs();
                download(url, jar, name);
            } else {
                if (!sha1.equals(DigestUtils.sha1(jar))) {
                    System.out.println("hash failed " + jar + " " + sha1);
                    // update
                    download(url, jar, name);
                }
            }
        }
        private void resolveArtifact(Artifact artifact, File jar, String name) throws IOException {
            resolve(artifact.getUrl(), artifact.getSha1(), jar, name);
        }

        private void resolveLibs(InstanceCfg instanceCfg, List<String> classpath, List<ResolvedNative> natives) throws IOException {
            for (Library library : instanceCfg.getLibraries()) {
                if(library.appliesToCurrentEnvironment((feature, enabled) -> {
                    throw new IllegalStateException(feature + " " + enabled);
                })) {ArtifactWithPath artifact = library.getDownloads().getArtifact();
                    if(artifact != null) {
                        if(!artifact.validatePath()) throw new IllegalStateException("bad path \"" + artifact.getPath() + "\"");
                        File jar = new File(libsDir, artifact.getPath());
                        resolveArtifact(artifact, jar, library.getName());
                        classpath.add(jar.getAbsolutePath());
                    }
                    String nativeLib = library.getNatives().get(Os.CURRENT_OS.getName());
                    if(nativeLib != null) {
                        ArtifactWithPath nativeArtifact = library.getDownloads().getClassifiers().get(nativeLib);
                        if(nativeArtifact == null) throw new NullPointerException(library.getName() + " " + nativeLib);
                        File jar = new File(libsDir, nativeArtifact.getPath());
                        resolveArtifact(nativeArtifact, jar, library.getName() + " " + nativeLib);
                        natives.add(new ResolvedNative(jar, library));
                    }
                }
            }
        }
        private File resolveGameJar(InstanceCfg instanceCfg) throws IOException {
            File gameJar = new File(instanceDir, instance.getId() + ".jar");
            Artifact client = instanceCfg.getDownloads().get("client");
            resolveArtifact(client, gameJar, instance.getId() + ".jar");
            return gameJar;
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

        private AssetsCfg resolveAssetsCfg(InstanceCfg instanceCfg) throws IOException {
            if(!assetsIndexesDir.exists()) assetsIndexesDir.mkdirs();
            File assetsIndexFile = new File(assetsIndexesDir, instanceCfg.getAssetIndex().getId() + ".json");
            resolveArtifact(instanceCfg.getAssetIndex(), assetsIndexFile, "assets index " + instanceCfg.getAssetIndex().getId());
            JsonElement json = Json.parse(TextUtils.readText(assetsIndexFile)).getAsJsonObject();
            if(json == null) throw new IllegalStateException("can't resolve instance config");
            return Json.GSON.fromJson(json, AssetsCfg.class);
        }

        private void resolveAssets(AssetsCfg assetsCfg) throws IOException {
            if(!assetsObjectsDir.exists()) assetsObjectsDir.mkdirs();
            for (Map.Entry<String, Asset> entry : assetsCfg.getObjects().entrySet()) {
                String name = entry.getKey();
                Asset asset = entry.getValue();
                String subHash = asset.getHash().substring(0, 2);
                String url = "https://resources.download.minecraft.net/" + subHash + "/" + asset.getHash();
                File hashDir = new File(assetsObjectsDir, subHash);
                if(!hashDir.exists()) hashDir.mkdir();
                File assetFile = new File(hashDir, asset.getHash());
                resolve(url, asset.getHash(), assetFile, "asset " + name);
            }
        }

        private File resolveLogging(InstanceCfg instanceCfg) throws IOException {
            LoggingClient client = instanceCfg.getLogging().getClient();
            File logCfg = new File(logConfigsDir, client.getFile().getId());
            resolve(client.getFile().getUrl(), client.getFile().getSha1(), logCfg, client.getFile().getId());
            return logCfg;
        }

        @Override protected Boolean call() throws Exception {
            if(profile == null) throw new IllegalStateException("profile is not selected");
            if(instance == null) throw new IllegalStateException("instance is not selected");
            if(!instanceDir.exists()) instanceDir.mkdirs();
            FileUtils.deleteDirectory(nativesDir);
            if(!nativesDir.exists()) nativesDir.mkdirs();


            InstanceCfg instanceCfg = resolveConfig();

            List<String> classpath = new ArrayList<>();
            List<ResolvedNative> natives = new ArrayList<>();
            resolveLibs(instanceCfg, classpath, natives);
            File gameJar = resolveGameJar(instanceCfg);
            classpath.add(gameJar.getAbsolutePath());
            unpackNatives(natives);


            // resolve assets
            AssetsCfg assetsCfg = resolveAssetsCfg(instanceCfg);
            resolveAssets(assetsCfg);

            // resolve logging
            File logsCfg = resolveLogging(instanceCfg);

            updateMessage("building args");
            // minecraft args
            Map<String, String> cfg = new HashMap<>();
            String classpathStr = String.join(File.pathSeparator, classpath);
            cfg.put("${classpath}", classpathStr);
            cfg.put("${game_jar}", gameJar.getAbsolutePath());
            cfg.put("${natives_directory}", nativesDir.getAbsolutePath());
            cfg.put("${game_assets}", assetsDir.getAbsolutePath());
            cfg.put("${assets_root}", assetsDir.getAbsolutePath());
            cfg.put("${auth_player_name}", profile.getName());
            cfg.put("${auth_uuid}", UuidAdapter.encode(profile.getUuid()));
            cfg.put("${auth_access_token}", profile.getAccessToken());
            cfg.put("${user_type}", "mojang");
            cfg.put("${user_properties}", "{}");
            cfg.put("${game_directory}", homeDir.getAbsolutePath());
            cfg.put("${assets_index_name}", instanceCfg.getAssetIndex().getId());
            cfg.put("${version_name}", instance.getId());
            cfg.put("${version_type}", instance.getType());
            cfg.put("${path}", logsCfg.getAbsolutePath());
            Manifest manifest = FileUtils.getManifest();
            if(manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                cfg.put("${launcher_name}", attributes.getValue("Implementation-Title"));
                cfg.put("${launcher_version}", attributes.getValue("Implementation-Version"));
            } else {
                cfg.put("${launcher_name}", "test");
                cfg.put("${launcher_version}", "0.1");
            }

            Map<String, Boolean> features = new HashMap<>();
            features.put("is_demo_user", false);
            features.put("has_custom_resolution", false);

            Arguments arguments = instanceCfg.getArguments();
            if(arguments.getJvm().isEmpty()) {
                List<ArgPart> jvm = new ArrayList<>();
                jvm.add(new RuleArg(Collections.singletonList(new Rule(Rule.Action.ALLOW, new Os(Os.Name.WINDOWS))), Collections.singletonList("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")));
                jvm.add(new RuleArg(Collections.singletonList(new Rule(Rule.Action.ALLOW, new Os(Os.Name.WINDOWS, "^10\\."))), Arrays.asList("-Dos.name=Windows 10", "-Dos.version=10.0")));
                jvm.add(new StringArg("-Djava.library.path=${natives_directory}"));
                jvm.add(new StringArg("-Dminecraft.launcher.brand=${launcher_name}"));
                jvm.add(new StringArg("-Dminecraft.launcher.version=${launcher_version}"));
                jvm.add(new StringArg("-cp"));
                jvm.add(new StringArg("${classpath}"));
                arguments = new Arguments(arguments.getGame(), jvm);
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

            jvm.add(new StringArg(instanceCfg.getLogging().getClient().getArgument()));


            List<String> args = new ArrayList<>();

            args.add("/usr/lib/jvm/jdk1.8.0_231/bin/java");
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

            args.add(instanceCfg.getMainClass());

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

            for (String arg : args) {
                System.out.println(arg);
            }
            updateMessage("starting minecraft");
            Process process = new ProcessBuilder(args)
                    .inheritIO()
                    .start();
            process.waitFor();

            return true;
        }

    };

    private ProgressController progress;
    private ProfileController profile;

    @Override protected void init(MVCApplication app) {
        progress = app.findController(ProgressController.class);
        profile = app.findController(ProfileController.class);
        progress.scheduleTask(loadCachedInstances.get());
    }

    private void updateInstances(List<Instance> instances) {
        this.instances.clear();
        List<Instance> filtered = new ArrayList<>();
        String type = instanceType.get();
        for (Instance instance : instances) {
            if(type != null && !instance.getType().equals(type)) continue;
            filtered.add(instance);
        }
        this.instances.addAll(filtered);
        if(selectedInstance.get() == null) {
            selectedInstance.set(CollectionEx.of(filtered).firstOrNull());
        }
    }

    private void loadData() {
        // TODO: load mcinfo

//        versionManifestUrl=https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/%1$s.json
//        librariesSource=https://libraries.minecraft.net/
//        jarUrl=https://s3.amazonaws.com/Minecraft.Download/versions/%1$s/%1$s.jar
//        assetsIndexUrl=https://s3.amazonaws.com/Minecraft.Download/indexes/%s.json
//        assetsSource=http://resources.download.minecraft.net/
//        yggdrasilAuthUrl=https://authserver.mojang.com/authenticate
//        resetPasswordUrl=https://minecraft.net/resetpassword
//
//        newsUrl=http://update.skcraft.com/template/news.html?version=%s
//        packageListUrl=http://update.skcraft.com/template/packages.json?key=%s
//        selfUpdateUrl=http://update.skcraft.com/template/launcher/latest.json
        // TODO: load user auth
    }


    public ObservableList<Instance> getInstances() {
        return instances;
    }

    public String getInstanceType() {
        return instanceType.get();
    }

    public SimpleObjectProperty<String> instanceTypeProperty() {
        return instanceType;
    }

    public Instance getSelectedInstance() {
        return selectedInstance.get();
    }

    public SimpleObjectProperty<Instance> selectedInstanceProperty() {
        return selectedInstance;
    }

    public void startMinecraft() {
        progress.scheduleTask(startMinecraft.get());
    }

    public void loadOnlineInstances() {
        progress.scheduleTask(loadOnlineInstances.get());
    }

}
