package de.dabbeljubee.blutdruckstatistik.Logic;


import android.content.res.Resources;
import org.joda.time.DateTime;

public abstract class MeasurementStatisticItem implements Comparable<Long> {

    private final DateTime firstDay;

    private final MeasurementStatistic systolic = new MeasurementStatistic(DataProvider.sysLevels);
    private final MeasurementStatistic diastolic = new MeasurementStatistic(DataProvider.diaLevels);
    private final MeasurementStatistic pulse = new MeasurementStatistic(DataProvider.pulseLevels);

    public abstract String getFirstDayAsString(Resources resources);

    MeasurementStatisticItem(DateTime firstDay) {
        this.firstDay = firstDay.withMillisOfDay(0);
    }

    @Override
    public int compareTo(Long o) {
        return Long.valueOf(firstDay.getMillis()).compareTo(o);
    }

    void addItem(int sysValue, int diaValue, int pulseValue) {
        systolic.addValue(sysValue);
        diastolic.addValue(diaValue);
        pulse.addValue(pulseValue);
    }

    void removeItem(int sysValue, int diaValue, int pulseValue) {
        systolic.removeValue(sysValue);
        diastolic.removeValue(diaValue);
        pulse.removeValue(pulseValue);
    }

    DateTime getFirstDay() {
        return firstDay;
    }

    public Long getFirstDayAsLong() {
        return firstDay.getMillis();
    }

    public MeasurementStatistic getSystolic() {
        return systolic;
    }

    public MeasurementStatistic getDiastolic() {
        return diastolic;
    }

    public MeasurementStatistic getPulse() {
        return pulse;
    }
}
