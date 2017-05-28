package de.dabbeljubee.blutdruckstatistik.Tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DateTimeGsonConverterTest {

    private static final DateTime dateTimeTestValue = ISODateTimeFormat.dateTime().parseDateTime("2017-02-16T12:34:56.123+01:00");
    private final DateTimeGsonConverter converter = new DateTimeGsonConverter();

    @Test
    public void testSerialize() {
        final JsonElement dateTime = converter.serialize(dateTimeTestValue, DateTime.class, null);
        assertThat(dateTime.getAsString(), is("2017-02-16T12:34"));
    }

    @Test
    public void testDeserialize() {
        final JsonElement jsonElement = new JsonPrimitive("2017-02-16T12:34");
        DateTime dateTime = (DateTime)converter.deserialize(jsonElement, DateTime.class, null);
        assertThat(dateTime, is(dateTimeTestValue.withSecondOfMinute(0).withMillisOfSecond(0)));
    }

}