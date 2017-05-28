package de.dabbeljubee.blutdruckstatistik.Logic;

import android.content.res.Resources;
import de.dabbeljubee.blutdruckstatistik.R;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import java.util.Locale;
import java.util.logging.Logger;

public class MeasurementStatisticWeek extends MeasurementStatisticItem {

    private static final Logger LOGGER = Logger.getLogger("MeasurementStatisticWeek");

    MeasurementStatisticWeek(DateTime firstDay) {
        super(firstDay.withDayOfWeek(DateTimeConstants.MONDAY));

        findMonthForWeek().addWeek(this);
    }

    private MeasurementStatisticMonth findMonthForWeek() {
        DateTime thursday = getFirstDay().withDayOfWeek(DateTimeConstants.THURSDAY).withZoneRetainFields(DateTimeZone.getDefault()).withTimeAtStartOfDay();
        DateTime firstOfMonth = thursday.withDayOfMonth(1);
        MeasurementStatisticMonth relatedMonth = DataProvider.getDataProvider().getMonthsMap().get(firstOfMonth.getMillis());
        if (null == relatedMonth) {
            relatedMonth = new MeasurementStatisticMonth(firstOfMonth);
            DataProvider.getDataProvider().getMonthsMap().put(firstOfMonth.getMillis(), relatedMonth);
            LOGGER.fine(String.format("Add Month  %s", firstOfMonth.toString()));
        }
        return relatedMonth;
    }

    @Override
    public String getFirstDayAsString(Resources resources) {
        return resources.getString(R.string.statistics_list_week_id, getFirstDay().getWeekOfWeekyear());
    }

    String getFirstDayAsString() {
        return String.format(Locale.GERMAN, "KW %02d", getFirstDay().getWeekOfWeekyear());
    }
}

