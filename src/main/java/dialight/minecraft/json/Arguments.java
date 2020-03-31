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

    private List<ArgPart> game;
    private List<ArgPart> jvm;
    private boolean complete;

    public Arguments() {
        game = new ArrayList<>();
        jvm = new ArrayList<>();
        complete = false;
    }
    public Arguments(List<ArgPart> game, List<ArgPart> jvm, boolean complete) {
        this.game = game;
        this.jvm = jvm;
        this.complete = complete;
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

    public void inherit(Arguments parent) {
        if(parent.complete) {
            this.game = parent.game;
            this.jvm = parent.jvm;
            this.complete = true;
            return;
        }

        if(game == null) game = new ArrayList<>();
        if(!(game instanceof ArrayList)) game = new ArrayList<>(game);
        if(parent.game != null) game.addAll(parent.game);

        if(jvm == null) jvm = new ArrayList<>();
        if(!(jvm instanceof ArrayList)) jvm = new ArrayList<>(jvm);
        if(parent.jvm != null) jvm.addAll(parent.jvm);
    }

    public boolean isComplete() {
        return this.complete;
    }

}
