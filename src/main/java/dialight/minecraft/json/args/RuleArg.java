package dialight.minecraft.json.args;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.json.libs.Rule;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RuleArg extends ArgPart {

    @SerializedName("rules")
    private final List<Rule> rules;

    @SerializedName("value")
    private final List<String> value;

    public RuleArg(List<Rule> rules, List<String> value) {
        this.rules = rules;
        this.value = value;
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
    @Override public List<String> bake(Function<String, String> keyMap, BiPredicate<String, Boolean> featureMatcher) {
        if (appliesToCurrentEnvironment(featureMatcher) && value != null)
            return value.stream()
                    .filter(Objects::nonNull)
                    .map(StringArg::new)
                    .map(str -> str.bake(keyMap))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

    public List<Rule> getRules() {
        return rules;
    }

    public List<String> getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value + " if " + rules;
    }
}
