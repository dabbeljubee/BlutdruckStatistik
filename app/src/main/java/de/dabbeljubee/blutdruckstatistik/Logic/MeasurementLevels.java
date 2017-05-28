package de.dabbeljubee.blutdruckstatistik.Logic;

public class MeasurementLevels {
    private int alarm;
    private int warning;
    private int low;

    MeasurementLevels(int alarm, int warning, int low) {
        this.alarm = alarm;
        this.warning = warning;
        this.low = low;
    }
    public int getAlarm() {
        return alarm;
    }
    void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public int getWarning() {
        return warning;
    }
    void setWarning(int warning) {
        this.warning = warning;
    }

    public int getLow() {
        return low;
    }
    void setLow(int low) {
        this.low = low;
    }
}
