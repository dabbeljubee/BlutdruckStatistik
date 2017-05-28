package de.dabbeljubee.blutdruckstatistik;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementData;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.junit.Test;

public class MainActivityTest {

    private MainActivity mainActivity = new MainActivity();

    @Test
    public void testAppendValueWithNulls() {
        assertThat(mainActivity.appendValue(null, null), is("0"));
    }

    @Test
    public void testAppendValueWithEmptyStrings() {
        assertThat(mainActivity.appendValue("", ""), is(""));
    }

    @Test
    public void testAppendValueWithEmptyValue() {
        assertThat(mainActivity.appendValue("1", ""), is("1"));
    }

    @Test
    public void testAppendValueWithEmptyValueToAdd() {
        assertThat(mainActivity.appendValue("", "1"), is("1"));
    }

    @Test
    public void testAppendValue() {
        assertThat(mainActivity.appendValue("1", "1"), is("11"));
    }

    @Test
    public void testAppendValueWithZeroValueToAdd() {
        assertThat(mainActivity.appendValue("0", "1"), is("10"));
    }

    @Test
    public void testAppendValueNoCascadingLeadingZeroes() {
        assertThat(mainActivity.appendValue("0", "0"), is("0"));
    }

    @Test
    public void testSaveMeasurementData() throws InterruptedException {
        final DataProvider.MeasurementsDataFormat measurementsMap = DataProvider.getDataProvider().getFilteredMeasurementsMap();

        mainActivity.saveMeasurementData(120, 80, 60);
        assertThat(measurementsMap.size(), is(1));

        measurementsMap.put(DateTime.now().minusMinutes(1).getMillis(), new MeasurementData(140, 100, 80));
        Thread.sleep(1);

        mainActivity.saveMeasurementData(130, 90, 70);
        assertThat(measurementsMap.size(), is(2));

        assertThat(measurementsMap.firstKey(), lessThan((measurementsMap.lastKey())));
        assertThat(measurementsMap.get(measurementsMap.firstKey()).getSystolicValue(), is(140));
        assertThat(measurementsMap.get(measurementsMap.lastKey()).getSystolicValue(), is(130));
    }
}
