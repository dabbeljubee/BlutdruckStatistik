package de.dabbeljubee.blutdruckstatistik.Logic;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDateTime;
import org.junit.Test;

public class MeasurementDataTest {

    @Test
    public void testSmallConstructor() {

        LocalDateTime before = LocalDateTime.now();
        MeasurementData measurementData = new MeasurementData(120, 80, 60);
        LocalDateTime after = LocalDateTime.now();

        assertThat(measurementData.getDateTime(), notNullValue());
        assertThat(measurementData.getSystolicValue(), is(120));
        assertThat(measurementData.getDiastolicValue(), is(80));
        assertThat(measurementData.getPulse(), is(60));
        assertThat(measurementData.isSaved(), is(false));
        assertThat(measurementData.getComment(), nullValue());
    }
}
