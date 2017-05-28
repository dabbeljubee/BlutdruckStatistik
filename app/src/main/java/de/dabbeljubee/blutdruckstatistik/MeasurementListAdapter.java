package de.dabbeljubee.blutdruckstatistik;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementData;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementLevels;

import java.util.List;

import static de.dabbeljubee.blutdruckstatistik.Tools.ColorMixer.determineColor;

public class MeasurementListAdapter extends ArrayAdapter<MeasurementData> {

    private static final String DATE_TIME_LIST_PATTERN = "yyyy-MM-dd HH:mm";
    private int resource;

    MeasurementListAdapter(Context context, int resource, List<MeasurementData> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TableRow measurementRow;

        final MeasurementData measurementData = getItem(position);

        if (null == convertView) {
            measurementRow = new TableRow(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater) getContext().getSystemService(inflater);
            layoutInflater.inflate(resource, measurementRow, true);
        } else {
            measurementRow = (TableRow) convertView;
        }

        TextView systolicValue = (TextView) measurementRow.findViewById(R.id.list_systolic_value);

        TextView dateTime = (TextView) measurementRow.findViewById(R.id.list_date_time);
        TextView diastolicValue = (TextView) measurementRow.findViewById(R.id.list_diastolic_value);
        TextView pulseValue = (TextView) measurementRow.findViewById(R.id.list_pulse_value);
        TextView comment = (TextView) measurementRow.findViewById(R.id.list_comment);

        dateTime.setText(measurementData.getDateTime().toString(DATE_TIME_LIST_PATTERN));
        diastolicValue.setText(String.valueOf(measurementData.getDiastolicValue()));
        pulseValue.setText(String.valueOf(measurementData.getPulse()));
        if (null != measurementData.getComment() && !measurementData.getComment().isEmpty()) {
            comment.setText("C");
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(measurementRow.getContext(), measurementData.getComment(), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        } else {
            comment.setText(" ");
        }

        formatDataItemToTextView(systolicValue, measurementData.getSystolicValue(), DataProvider.sysLevels);
        formatDataItemToTextView(diastolicValue, measurementData.getDiastolicValue(), DataProvider.diaLevels);
        formatDataItemToTextView(pulseValue, measurementData.getPulse(), DataProvider.pulseLevels);

        return measurementRow;
    }

    private void formatDataItemToTextView(TextView textView, int value, MeasurementLevels measurementLevels) {
        textView.setText(String.valueOf(value));
        final int alarmColor = Color.RED;
        final int warningColor = 0xFFFF8000;
        final int normalColor = 0xFF00CC00;
        final int lowColor = Color.BLUE;

        final int normal = (measurementLevels.getLow() + measurementLevels.getWarning()) / 2;

        if (measurementLevels.getAlarm() < value) {
            textView.setTextColor(alarmColor);
            return;
        }
        if (measurementLevels.getWarning() < value) {
            textView.setTextColor(determineColor(value, measurementLevels.getWarning(), measurementLevels.getAlarm(), alarmColor, warningColor));
            return;
        }
        if (normal < value) {
            textView.setTextColor(determineColor(value, normal, measurementLevels.getWarning(), warningColor, normalColor));
            return;
        }
        if (measurementLevels.getLow() < value) {
            textView.setTextColor(determineColor(value, measurementLevels.getLow(), normal, normalColor, lowColor));
            return;
        }
        textView.setTextColor(lowColor);
    }
}
