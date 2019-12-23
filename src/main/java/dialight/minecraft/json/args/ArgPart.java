package dialight.minecraft.json.args;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public abstract class ArgPart {
    public abstract List<String> bake(Function<String, String> keyMap, BiPredicate<String, Boolean> featureMatcher);
}
