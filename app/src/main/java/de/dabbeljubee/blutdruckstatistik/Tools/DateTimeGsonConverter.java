package de.dabbeljubee.blutdruckstatistik.Tools;

import com.google.gson.*;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementData;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.lang.reflect.Type;

public class DateTimeGsonConverter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

    @Override
    public JsonElement serialize(DateTime dateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dateTime.toString(DateTimeFormat.forPattern(MeasurementData.DATE_TIME_UTC_PATTERN)));
    }

    @Override
    public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        return DateTime.parse(json.getAsString());
    }
}
