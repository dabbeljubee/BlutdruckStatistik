package de.dabbeljubee.blutdruckstatistik.Logic;

import android.content.res.Resources;
import org.joda.time.DateTime;

import java.util.TreeMap;

public class MeasurementStatisticMonth extends MeasurementStatisticItem {

    private TreeMap<Long, MeasurementStatisticWeek> relatedWeeks = new TreeMap<>();

    MeasurementStatisticMonth(DateTime firstDay) {
        super(firstDay.withDayOfMonth(1));
    }

    void addWeek(MeasurementStatisticWeek week) {
        MeasurementStatisticWeek relatedWeek = relatedWeeks.get(week.getFirstDay().getMillis());
        if (null == relatedWeek) {
            relatedWeeks.put(week.getFirstDay().getMillis(), week);
        }
    }

    @Override
    public String getFirstDayAsString(Resources resources) {
        return getFirstDayAsString();
    }

    String getFirstDayAsString() {
        return getFirstDay().toString("YYYY-MM");
    }

    public TreeMap<Long, MeasurementStatisticWeek> getRelatedWeeks() {
        return relatedWeeks;
    }
}
