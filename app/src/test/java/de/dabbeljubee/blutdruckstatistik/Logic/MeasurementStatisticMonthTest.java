package de.dabbeljubee.blutdruckstatistik.Logic;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MeasurementStatisticMonthTest {

    @Test
    public void testDateOutput() {
        MeasurementStatisticMonth measurementStatisticMonth = new MeasurementStatisticMonth(DateTime.parse("20170121", ISODateTimeFormat.basicDate()));
        assertThat(measurementStatisticMonth.getFirstDayAsString(), is("2017-01"));
    }
}
