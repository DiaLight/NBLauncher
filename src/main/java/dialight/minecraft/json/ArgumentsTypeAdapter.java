package dialight.minecraft.json;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dialight.minecraft.json.args.ArgPart;
import dialight.minecraft.json.args.RuleArg;
import dialight.minecraft.json.args.StringArg;
import dialight.minecraft.json.libs.Rule;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArgumentsTypeAdapter implements JsonDeserializer<Arguments> {

    public static final ArgumentsTypeAdapter INSTANCE = new ArgumentsTypeAdapter();

    @Override public Arguments deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return new Arguments(
                    Arrays.stream(json.getAsString().split(" "))
                            .map(StringArg::new)
                            .collect(Collectors.toList()),
                    Collections.emptyList(),
                    true
            );
        }
        JsonObject arguments = json.getAsJsonObject();
        List<ArgPart> game;
        if(arguments.has("game")) {
            game = parseArgParts(context, arguments.getAsJsonArray("game"));
        } else {
            game = Collections.emptyList();
        }
        List<ArgPart> jvm;
        if (arguments.has("jvm")) {
            jvm = parseArgParts(context, arguments.getAsJsonArray("jvm"));
        } else {
            jvm = Collections.emptyList();
        }
        return new Arguments(game, jvm, false);
    }

    private List<ArgPart> parseArgParts(JsonDeserializationContext ctx, JsonArray gameJes) {
        List<ArgPart> game;
        game = new ArrayList<>();
        for (JsonElement je : gameJes) {
            if (je.isJsonPrimitive()) {
                game.add(new StringArg(je.getAsString()));
            } else {
                JsonObject gamePart = je.getAsJsonObject();

                List<Rule> rules = ctx.deserialize(gamePart.get("rules"), new TypeToken<List<Rule>>() {}.getType());

                List<String> part;
                JsonElement value = gamePart.get("value");
                if(value.isJsonPrimitive()) {
                    part = Collections.singletonList(value.getAsString());
                } else {
                    part = new ArrayList<>();
                    for (JsonElement partJe : value.getAsJsonArray()) {
                        part.add(partJe.getAsString());
                    }
                }

                game.add(new RuleArg(rules, part));
            }
        }
        return game;
    }

}
