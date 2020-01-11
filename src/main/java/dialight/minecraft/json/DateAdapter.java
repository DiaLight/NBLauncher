package dialight.minecraft.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {

    public static final DateAdapter INSTANCE = new DateAdapter();

    private static final DateFormat EN_US_FORMAT = DateFormat.getDateTimeInstance(2, 2, Locale.US);
    private static final DateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static Date deserializeToDate(String string) {
        synchronized(EN_US_FORMAT) {
            try {
                return EN_US_FORMAT.parse(string);
            } catch (ParseException ex1) {
                try {
                    return ISO_8601_FORMAT.parse(string);
                } catch (ParseException ex2) {
                    try {
                        String cleaned = string.replace("Z", "+00:00");
                        cleaned = cleaned.substring(0, 22) + cleaned.substring(23);
                        return ISO_8601_FORMAT.parse(cleaned);
                    } catch (Exception e) {
                        throw new JsonParseException("Invalid date: " + string, e);
                    }
                }
            }
        }
    }
    public static String serializeFromDate(Date date) {
        return ISO_8601_FORMAT.format(date);
    }

    @Override public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(serializeFromDate(src));
    }

    @Override public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserializeToDate(json.getAsString());
    }

}
