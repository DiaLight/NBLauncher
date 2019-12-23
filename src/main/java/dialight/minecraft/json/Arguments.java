package dialight.minecraft.json;


import dialight.minecraft.json.args.ArgPart;
import dialight.minecraft.json.args.RuleArg;
import dialight.minecraft.json.args.StringArg;
import dialight.minecraft.json.libs.Os;
import dialight.minecraft.json.libs.Rule;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Arguments {

    private final List<ArgPart> game;
    private final List<ArgPart> jvm;

    public Arguments(List<ArgPart> game, List<ArgPart> jvm) {
        this.game = game;
        this.jvm = jvm;
    }

    public static List<String> bakeArgs(List<ArgPart> arguments, Function<String, String> keyMap, BiPredicate<String, Boolean> featureMatcher) {
        return arguments.stream()
                .flatMap(arg -> arg.bake(keyMap, featureMatcher).stream())
                .collect(Collectors.toList());
    }

    public List<String> bakeJvmArgs(Function<String, String> keyMap, BiPredicate<String, Boolean> featureMatcher) {
        return bakeArgs(jvm, keyMap, featureMatcher);
    }

    public List<String> bakeGameArgs(Function<String, String> keyMap, BiPredicate<String, Boolean> featureMatcher) {
        return bakeArgs(game, keyMap, featureMatcher);
    }

    public List<ArgPart> getJvm() {
        return jvm;
    }

    public List<ArgPart> getGame() {
        return game;
    }

    public void dump() {
        for (ArgPart part : game) {
            System.out.println(part);
        }
        for (ArgPart part : jvm) {
            System.out.println(part);
        }
    }

}
