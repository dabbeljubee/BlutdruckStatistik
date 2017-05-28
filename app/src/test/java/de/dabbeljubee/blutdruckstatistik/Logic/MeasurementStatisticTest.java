package de.dabbeljubee.blutdruckstatistik.Logic;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.math.BigDecimal;

public class MeasurementStatisticTest {

    MeasurementStatistic measurementStatistic = new MeasurementStatistic(DataProvider.sysLevels);

    @Test
    public void testEmpty() {
        assertThat(measurementStatistic.getValueCount(), is(BigDecimal.ZERO));
        assertThat(measurementStatistic.getAlarmCount(), is(0));
        assertThat(measurementStatistic.getWarningCount(), is(0));
        assertThat(measurementStatistic.getNormalCount(), is(0));
        assertThat(measurementStatistic.getLowCount(),is(0));

        assertThat(measurementStatistic.getAlarmRatio(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(measurementStatistic.getWarningRatio(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(measurementStatistic.getNormalRatio(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(measurementStatistic.getLowRatio(), comparesEqualTo(BigDecimal.ZERO));
    }

    @Test
    public void testWithAlarmValue() {
        measurementStatistic.addValue(DataProvider.sysLevels.getAlarm() + 1);

        assertThat(measurementStatistic.getValueCount(), is(BigDecimal.ONE));
        assertThat(measurementStatistic.getAlarmCount(), is(100));
        assertThat(measurementStatistic.getWarningCount(), is(0));
        assertThat(measurementStatistic.getNormalCount(), is(0));
        assertThat(measurementStatistic.getLowCount(),is(0));

        assertThat(measurementStatistic.getAlarmRatio(), comparesEqualTo(BigDecimal.valueOf(100)));
        assertThat(measurementStatistic.getWarningRatio(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(measurementStatistic.getNormalRatio(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(measurementStatistic.getLowRatio(), comparesEqualTo(BigDecimal.ZERO));
    }

    @Test
    public void testWithWarningValue() {
        measurementStatistic.addValue(DataProvider.sysLevels.getAlarm());

        assertThat(measurementStatistic.getValueCount(), is(BigDecimal.ONE));
        assertThat(measurementStatistic.getAlarmCount(), is(0));
        assertThat(measurementStatistic.getWarningCount(), is(100));
        assertThat(measurementStatistic.getNormalCount(), is(0));
        assertThat(measurementStatistic.getLowCount(),is(0));

        assertThat(measurementStatistic.getAlarmRatio(), comparesEqualTo(BigDecimal.valueOf(0)));
        assertThat(measurementStatistic.getWarningRatio(), comparesEqualTo(BigDecimal.valueOf(100)));
        assertThat(measurementStatistic.getNormalRatio(), comparesEqualTo(BigDecimal.ZERO));
        assertThat(measurementStatistic.getLowRatio(), comparesEqualTo(BigDecimal.ZERO));
    }

    @Test
    public void testWithNormalValue() {
        measurementStatistic.addValue(DataProvider.sysLevels.getWarning());

        assertThat(measurementStatistic.getValueCount(), is(BigDecimal.ONE));
        assertThat(measurementStatistic.getAlarmCount(), is(0));
        assertThat(measurementStatistic.getWarningCount(), is(0));
        assertThat(measurementStatistic.getNormalCount(), is(100));
        assertThat(measurementStatistic.getLowCount(),is(0));

        assertThat(measurementStatistic.getAlarmRatio(), comparesEqualTo(BigDecimal.valueOf(0)));
        assertThat(measurementStatistic.getWarningRatio(), comparesEqualTo(BigDecimal.valueOf(0)));
        assertThat(measurementStatistic.getNormalRatio(), comparesEqualTo(BigDecimal.valueOf(100)));
        assertThat(measurementStatistic.getLowRatio(), comparesEqualTo(BigDecimal.ZERO));
    }

    @Test
    public void testWithUnderValue() {
        measurementStatistic.addValue(DataProvider.sysLevels.getLow());

        assertThat(measurementStatistic.getValueCount(), is(BigDecimal.ONE));
        assertThat(measurementStatistic.getAlarmCount(), is(0));
        assertThat(measurementStatistic.getWarningCount(), is(0));
        assertThat(measurementStatistic.getNormalCount(), is(0));
        assertThat(measurementStatistic.getLowCount(),is(100));

        assertThat(measurementStatistic.getAlarmRatio(), comparesEqualTo(BigDecimal.valueOf(0)));
        assertThat(measurementStatistic.getWarningRatio(), comparesEqualTo(BigDecimal.valueOf(0)));
        assertThat(measurementStatistic.getNormalRatio(), comparesEqualTo(BigDecimal.valueOf(0)));
        assertThat(measurementStatistic.getLowRatio(), comparesEqualTo(BigDecimal.valueOf(100)));
    }

    @Test
    public void testWithAllValues() {
        measurementStatistic.addValue(DataProvider.sysLevels.getAlarm() + 1);
        measurementStatistic.addValue(DataProvider.sysLevels.getWarning() + 1);
        measurementStatistic.addValue(DataProvider.sysLevels.getLow() + 1);
        measurementStatistic.addValue(DataProvider.sysLevels.getLow() -1);

        assertThat(measurementStatistic.getValueCount(), is(BigDecimal.valueOf(4)));
        assertThat(measurementStatistic.getAlarmCount(), is(100));
        assertThat(measurementStatistic.getWarningCount(), is(100));
        assertThat(measurementStatistic.getNormalCount(), is(100));
        assertThat(measurementStatistic.getLowCount(),is(100));

        assertThat(measurementStatistic.getAlarmRatio(), comparesEqualTo(BigDecimal.valueOf(25)));
        assertThat(measurementStatistic.getWarningRatio(), comparesEqualTo(BigDecimal.valueOf(25)));
        assertThat(measurementStatistic.getNormalRatio(), comparesEqualTo(BigDecimal.valueOf(25)));
        assertThat(measurementStatistic.getLowRatio(), comparesEqualTo(BigDecimal.valueOf(25)));
    }
}
