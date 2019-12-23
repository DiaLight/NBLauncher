package dialight.minecraft.json.libs;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Rule {

    @SerializedName("action")
    private final Action action;

    @SerializedName("os")
    private final Os os;

    @SerializedName("features")
    private final Map<String, Boolean> features;

    public Rule(Action action, Os os) {
        this(action, os, Collections.emptyMap());
    }
    public Rule(Action action, Os os, Map<String, Boolean> features) {
        this.action = action;
        this.os = os;
        this.features = features;
    }

    public Action getAppliedAction(BiPredicate<String, Boolean> featureMatcher) {
        if (os != null && !os.allow()) return null;
        if (features != null) {
            for (Map.Entry<String, Boolean> entry : features.entrySet()) {
                if (!featureMatcher.test(entry.getKey(), entry.getValue())) return null;
            }
        }
        return action;
    }

    public Action getAction() {
        return action;
    }

    public Os getOs() {
        return os;
    }

    public Map<String, Boolean> getFeatures() {
        return features;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "action='" + action + '\'' +
                ", os=" + os +
                ", features=" + features +
                '}';
    }

    public enum Action {
        ALLOW,
        DISALLOW;

        public static class Serialize implements JsonDeserializer<Action> {

            public static final Action.Serialize INSTANCE = new Action.Serialize();

            @Override public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Action.valueOf(json.getAsString().toUpperCase());
            }
        }
    }

}
