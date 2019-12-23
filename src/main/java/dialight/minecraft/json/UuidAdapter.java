package dialight.minecraft.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

public class UuidAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

    public static final UuidAdapter INSTANCE = new UuidAdapter();

    public static String encode(UUID value) {
        return value.toString().replace("-", "");
    }
    public static UUID decode(String value) {
        return UUID.fromString(value.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    @Override public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(encode(src));
    }

    @Override public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return decode(json.getAsString());
    }

}
