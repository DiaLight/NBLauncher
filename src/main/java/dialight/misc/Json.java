package dialight.misc;

import com.google.gson.*;
import dialight.minecraft.json.Arguments;
import dialight.minecraft.json.ArgumentsTypeAdapter;
import dialight.minecraft.json.DateAdapter;
import dialight.minecraft.json.UuidAdapter;
import dialight.minecraft.json.libs.Os;
import dialight.minecraft.json.libs.Rule;

import java.util.Date;
import java.util.UUID;


public class Json {

    private static final JsonParser JSON_PARSER = new JsonParser();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class, UuidAdapter.INSTANCE)
            .registerTypeAdapter(Date.class, DateAdapter.INSTANCE)
            .registerTypeAdapter(Arguments.class, ArgumentsTypeAdapter.INSTANCE)
            .registerTypeAdapter(Rule.Action.class, Rule.Action.Serialize.INSTANCE)
            .registerTypeAdapter(Os.Name.class, Os.Name.Serialize.INSTANCE)
            .create();

    // Json.GSON.fromJson(lib.getAsJsonObject("natives"), new TypeToken<Map<String, String>>() {}.getType());

    public static JsonElement parse(String text) {
        return JSON_PARSER.parse(text);
    }

    public static JsonElement build(Object obj) {
        return GSON.toJsonTree(obj);
    }

}
