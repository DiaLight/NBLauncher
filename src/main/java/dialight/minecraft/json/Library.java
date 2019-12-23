package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.json.libs.Downloads;
import dialight.minecraft.json.libs.Extract;
import dialight.minecraft.json.libs.Rule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class Library {

    @SerializedName("name")
    private final String name;

    @SerializedName("downloads")
    private final Downloads downloads;

    @SerializedName("natives")
    private final Map<String, String> natives;

    @SerializedName("extract")
    private final Extract extract;

    @SerializedName("rules")
    private final List<Rule> rules;

    public Library() {
        this(null);
    }
    public Library(String name) {
        this(name, null);
    }

    public Library(String name, Downloads downloads) {
        this(name, downloads, Collections.emptyMap());
    }

    public Library(String name, Downloads downloads, Map<String, String> natives) {
        this(name, downloads, natives, null);
    }

    public Library(String name, Downloads downloads, Map<String, String> natives, Extract extract) {
        this(name, downloads, natives, extract, Collections.emptyList());
    }

    public Library(String name, Downloads downloads, Map<String, String> natives, Extract extract, List<Rule> rules) {
        this.name = name;
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
        return natives;
    }

    public Extract getExtract() {
        return extract;
    }

    public List<Rule> getRules() {
        return rules;
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
