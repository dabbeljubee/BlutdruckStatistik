package de.dabbeljubee.blutdruckstatistik.Logic;


import org.joda.time.DateTime;

public class MeasurementData {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm";
    static final String DATE_TIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;
    public static final String DATE_TIME_UTC_PATTERN = DATE_PATTERN + "'T'" + TIME_PATTERN;

    private final DateTime dateTime;

    private int systolicValue;
    private int diastolicValue;
    private int pulse;

    private boolean saved = false;
    private String comment;

    public MeasurementData(DateTime dateTime, int systolicValue, int diastolicValue, int pulse, boolean saved, String comment) {
        this.dateTime = dateTime.withSecondOfMinute(0).withMillisOfSecond(0);
        this.systolicValue = systolicValue;
        this.diastolicValue = diastolicValue;
        this.pulse = pulse;
        this.saved = saved;
        this.comment = comment;
    }

    public MeasurementData(int systolicValue, int diastolicValue, int pulse) {
        this(DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0), systolicValue, diastolicValue, pulse, false, null);
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public String formatDateTimeAsString() {
        return dateTime.toString(MeasurementData.DATE_TIME_PATTERN);
    }

    public int getSystolicValue() {
        return systolicValue;
    }

    public int getDiastolicValue() {
        return diastolicValue;
    }

    public int getPulse() {
        return pulse;
    }

    boolean isSaved() {
        return saved;
    }

    public String getComment() {
        return comment;
    }

    String getCommentNotNull() {
        return null == comment ? "" : comment;
    }
}
