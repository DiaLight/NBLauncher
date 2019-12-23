package dialight.minecraft.json.args;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringArg extends ArgPart {

    private final String string;

    public StringArg(String string) {
        this.string = string;
    }

    @Override public String toString() {
        return string;
    }

    public String bake(Function<String, String> keyMap) {
        String res = string;
        Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
        Matcher m = pattern.matcher(string);
        while (m.find()) {
            String entry = m.group();
            res = res.replace(entry, keyMap.apply(entry));
        }
        return res;
    }

    @Override public List<String> bake(Function<String, String> keyMap, BiPredicate<String, Boolean> featureMatcher) {
        return Collections.singletonList(bake(keyMap));
    }

}
