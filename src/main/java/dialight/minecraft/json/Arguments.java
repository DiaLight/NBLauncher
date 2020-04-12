package dialight.minecraft.json;


import dialight.minecraft.json.args.ArgPart;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Arguments {

    private List<ArgPart> game;
    private List<ArgPart> jvm;
    private boolean legacy;

    public Arguments(boolean legacy) {
        this.game = new ArrayList<>();
        this.jvm = new ArrayList<>();
        this.legacy = legacy;
    }
    public Arguments(List<ArgPart> game, List<ArgPart> jvm, boolean legacy) {
        this.game = game;
        this.jvm = jvm;
        this.legacy = legacy;
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
        if(parent.legacy) {
            this.game = parent.game;
            this.jvm = parent.jvm;
            this.legacy = true;
            return;
        }

        if(game == null) game = new ArrayList<>();
        if(!(game instanceof ArrayList)) game = new ArrayList<>(game);
        if(parent.game != null) game.addAll(parent.game);

        if(jvm == null) jvm = new ArrayList<>();
        if(!(jvm instanceof ArrayList)) jvm = new ArrayList<>(jvm);
        if(parent.jvm != null) jvm.addAll(parent.jvm);
    }

    public boolean isLegacy() {
        return this.legacy;
    }

}
