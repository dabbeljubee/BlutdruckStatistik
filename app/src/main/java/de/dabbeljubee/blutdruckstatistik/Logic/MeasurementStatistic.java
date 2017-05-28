package de.dabbeljubee.blutdruckstatistik.Logic;


import java.math.BigDecimal;
import java.util.logging.Logger;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.RoundingMode.HALF_UP;

public class MeasurementStatistic {

    private static final Logger LOGGER = Logger.getLogger(MeasurementStatistic.class.getName());

    private MeasurementLevels measurementLevels;

    private int valueCount = 0;

    private int alarmCount = 0;
    private int warningCount = 0;
    private int normalCount = 0;
    private int lowCount = 0;

    MeasurementStatistic(MeasurementLevels measurementLevels) {
        this.measurementLevels = measurementLevels;
    }

    void addValue(final int value) {
        valueCount++;
        if (measurementLevels.getAlarm() < value) {
            alarmCount += 100;
            return;
        }
        if (measurementLevels.getWarning() < value) {
            warningCount += 100;
            return;
        }
        if (measurementLevels.getLow() < value) {
            normalCount += 100;
            return;
        }
        lowCount += 100;
    }

    void removeValue(final int value) {
        valueCount = Math.max(valueCount - 1, 0);
        if (measurementLevels.getAlarm() < value) {
            LOGGER.fine(String.format("Remove from ALARM: %d", value));
            alarmCount = Math.max(alarmCount - 100, 0);
            return;
        }
        if (measurementLevels.getWarning() < value) {
            LOGGER.fine(String.format("Remove from WARNING: %d", value));
            warningCount = Math.max(warningCount - 100, 0);
            return;
        }
        if (measurementLevels.getLow() < value) {
            LOGGER.fine(String.format("Remove from NORMAL: %d", value));
            normalCount = Math.max(normalCount - 100, 0);
            return;
        }
        LOGGER.fine(String.format("Remove from LOW: %d", value));
        lowCount = Math.max(lowCount - 100, 0);
    }

    BigDecimal getValueCount() {
        return BigDecimal.valueOf(valueCount);
    }

    int getAlarmCount() {
        return alarmCount;
    }

    int getWarningCount() {
        return warningCount;
    }

    int getNormalCount() {
        return normalCount;
    }

    int getLowCount() {
        return lowCount;
    }

    public BigDecimal getAlarmRatio() {
        return calculateRatio(alarmCount);
    }

    public BigDecimal getWarningRatio() {
        return calculateRatio(warningCount);
    }

    public BigDecimal getNormalRatio() {
        return calculateRatio(normalCount);
    }

    public BigDecimal getLowRatio() {
        return calculateRatio(lowCount);
    }

    private BigDecimal calculateRatio(int value)  {
        try {
            return BigDecimal.valueOf(value).setScale(1, HALF_UP).divide(getValueCount(), ROUND_HALF_UP);
        } catch (ArithmeticException ae) {
            return BigDecimal.ZERO.setScale(1, HALF_UP);
        }
    }
}
