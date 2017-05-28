package de.dabbeljubee.blutdruckstatistik.Logic;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MeasurementStatisticWeekTest {

    @Test
    public void testDateOutput() {
        MeasurementStatisticWeek measurementStatisticWeek = new MeasurementStatisticWeek(DateTime.parse("2017-01-21"));
        assertThat(measurementStatisticWeek.getFirstDayAsString(), is("KW 03"));
    }
}
