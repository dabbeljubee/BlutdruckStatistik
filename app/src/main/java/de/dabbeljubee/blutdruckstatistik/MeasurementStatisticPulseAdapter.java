package de.dabbeljubee.blutdruckstatistik;

import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementStatisticItem;

public class MeasurementStatisticPulseAdapter extends MeasurementStatisticAdapter {

    @Override
    protected void formatHeader(View rootView) {

        TextView headerTime = (TextView) rootView.findViewById(R.id.statistic_header_time);
        headerTime.setText(R.string.statistics_header_time);

        TextView header1 = (TextView) rootView.findViewById(R.id.statistic_header_type_1);
        header1.setText(R.string.statistics_header_type_pulse);
        TextView header2 = (TextView) rootView.findViewById(R.id.statistic_header_type_2);
        header2.setText(R.string.statistics_header_type_pulse);
        TextView header3 = (TextView) rootView.findViewById(R.id.statistic_header_type_3);
        header3.setText(R.string.statistics_header_type_pulse);
        TextView header4 = (TextView) rootView.findViewById(R.id.statistic_header_type_4);
        header4.setText(R.string.statistics_header_type_pulse);

        TextView level1 = (TextView) rootView.findViewById(R.id.statistic_header_level_1);
        level1.setText(rootView.getResources().getString(
                R.string.statistics_header_level_less, DataProvider.pulseLevels.getAlarm()));
        TextView level2 = (TextView) rootView.findViewById(R.id.statistic_header_level_2);
        level2.setText(rootView.getResources().getString(
                R.string.statistics_header_level_less, DataProvider.pulseLevels.getWarning()));
        TextView level3 = (TextView) rootView.findViewById(R.id.statistic_header_level_3);
        level3.setText(rootView.getResources().getString(
                R.string.statistics_header_level_less, DataProvider.pulseLevels.getLow()));
        TextView level4 = (TextView) rootView.findViewById(R.id.statistic_header_level_4);
        level4.setText(rootView.getResources().getString(
                R.string.statistics_header_level_greater, DataProvider.pulseLevels.getLow()));
    }


    @Override
    protected void fillRowWithData(TableRow row, MeasurementStatisticItem item) {
        ((TextView) row.findViewById(R.id.statistic_over_alarm)).setText(item.getPulse().getAlarmRatio().toPlainString());
        ((TextView) row.findViewById(R.id.statistic_over_warning)).setText(item.getPulse().getWarningRatio().toPlainString());
        ((TextView) row.findViewById(R.id.statistic_over_low)).setText(item.getPulse().getNormalRatio().toPlainString());
        ((TextView) row.findViewById(R.id.statistic_under_low)).setText(item.getPulse().getLowRatio().toPlainString());
    }
}
