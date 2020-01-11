package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.LibName;
import dialight.minecraft.json.libs.Downloads;
import dialight.minecraft.json.libs.Extract;
import dialight.minecraft.json.libs.Rule;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class Library {

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("downloads")
    private Downloads downloads;

    @SerializedName("natives")
    private Map<String, String> natives;

    @SerializedName("extract")
    private Extract extract;

    @SerializedName("rules")
    private List<Rule> rules;

    @SerializedName("downloadOnly")
    private boolean downloadOnly = false;

    @SerializedName("loadLast")
    private boolean loadLast = false;

    public Library() {

    }

    public Library(String name, String url, Downloads downloads, Map<String, String> natives, Extract extract, List<Rule> rules) {
        this.name = name;
        this.url = url;
        this.downloads = downloads;
        this.natives = natives;
        this.extract = extract;
        this.rules = rules;
    }

    public boolean appliesToCurrentEnvironment(BiPredicate<String, Boolean> featureMatcher) {
        if (rules == null || rules.isEmpty()) return true;

        Rule.Action action = Rule.Action.DISALLOW;
        for (Rule rule : rules) {
            Rule.Action curAction = rule.getAppliedAction(featureMatcher);
            if (curAction != null) {
                action = curAction;
            }
        }

        return action == Rule.Action.ALLOW;
    }

    public String getName() {
        return name;
    }

    public Downloads getDownloads() {
        return downloads;
    }

    public Map<String, String> getNatives() {
        if(natives == null) return Collections.emptyMap();
        return natives;
    }

    public Extract getExtract() {
        return extract;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public boolean isDownloadOnly() {
        return downloadOnly;
    }

    public boolean isLoadLast() {
        return loadLast;
    }

    public String resolvePath() {
        if(downloads != null) {
            ArtifactWithPath artifact = downloads.getArtifact();
            if(artifact != null) {
                String path = artifact.getPath();
                if(path != null) {
                    if(!artifact.validatePath()) throw new IllegalStateException("bad path \"" + path + "\"");
                    return path;
                }
            }
        }
        LibName name = LibName.parse(this.name);
        return name.buildPath();
    }

    @Nullable public String resolveUrl() {
        if(downloads != null) {
            ArtifactWithPath artifact = downloads.getArtifact();
            if(artifact != null) {
                String url = artifact.getUrl();
                if(url != null) return url;
            }
        }
        if(url != null) {
            if(url.endsWith("/")) {
                return url + resolvePath();
            }
            return url + "/" + resolvePath();
        }
        return null;
    }

    public int resolveSize() {
        if(downloads != null) {
            ArtifactWithPath artifact = downloads.getArtifact();
            if(artifact != null) {
                return artifact.getSize();
            }
        }
        return 0;
    }

    @Nullable public String resolveSha1() {
        if(downloads != null) {
            ArtifactWithPath artifact = downloads.getArtifact();
            if(artifact != null) {
                return artifact.getSha1();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Library{" +
                "name=" + name +
                ", downloads=" + downloads +
                ", natives=" + natives +
                ", extract=" + extract +
                ", rules=" + rules +
                '}';
    }
}
