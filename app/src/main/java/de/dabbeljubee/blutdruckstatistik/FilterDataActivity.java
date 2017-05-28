package de.dabbeljubee.blutdruckstatistik;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.*;
import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementData;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.base.BaseLocal;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

public class FilterDataActivity extends AppCompatActivity {

    private final static Logger LOGGER = Logger.getLogger("FilterDataActivity");
    private final static SparseIntArray weekdayArray = new SparseIntArray();
    static
    {
        weekdayArray.put(R.id.filter_show_monday, DateTimeConstants.MONDAY);
        weekdayArray.put(R.id.filter_show_tuesday, DateTimeConstants.TUESDAY);
        weekdayArray.put(R.id.filter_show_wednesday, DateTimeConstants.WEDNESDAY);
        weekdayArray.put(R.id.filter_show_thursday, DateTimeConstants.THURSDAY);
        weekdayArray.put(R.id.filter_show_friday, DateTimeConstants.FRIDAY);
        weekdayArray.put(R.id.filter_show_saturday, DateTimeConstants.SATURDAY);
        weekdayArray.put(R.id.filter_show_sunday, DateTimeConstants.SUNDAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_data);

        final long datetime = getIntent().getLongExtra("datetime", 0);
        LOGGER.fine(String.format(Locale.GERMAN, "Data: %d", datetime));

        LocalDate startDate = LocalDate.now().minusMonths(3);
        if (null != DataProvider.getStartDate()) startDate = DataProvider.getStartDate();
        formatDateOrTime(R.id.filter_data_start_date_check, R.id.filter_data_start_date, startDate, null != DataProvider.getStartDate());

        LocalDate endDate = LocalDate.now();
        if (null != DataProvider.getEndDate()) endDate = DataProvider.getEndDate();
        formatDateOrTime(R.id.filter_data_end_date_check, R.id.filter_data_end_date, endDate, null != DataProvider.getEndDate());

        LocalTime startTime = LocalTime.MIDNIGHT;
        if (null != DataProvider.getStartTime()) startTime = DataProvider.getStartTime();
        formatDateOrTime(R.id.filter_data_start_time_check, R.id.filter_data_start_time, startTime, null != DataProvider.getStartTime());

        LocalTime endTime = LocalTime.MIDNIGHT.minusMinutes(1);
        if (null != DataProvider.getEndTime()) endTime = DataProvider.getEndTime();
        formatDateOrTime(R.id.filter_data_end_time_check, R.id.filter_data_end_time, endTime, null != DataProvider.getEndTime());

        final CheckBox checkBox = (CheckBox) findViewById(R.id.filter_show_all_weekdays);
        checkBox.setChecked(7 == DataProvider.getActiveWeekdays().size());
        for (int i = 0; i < weekdayArray.size(); i++) {
            formatWeekdayCheckBox(weekdayArray.keyAt(i), weekdayArray.valueAt(i));
        }
    }

    private void formatDateOrTime(int checkBoxId, int editTextId, BaseLocal dateOrTime, boolean filterIsActive) {
        final CheckBox checkBox = (CheckBox) findViewById(checkBoxId);
        checkBox.setChecked(filterIsActive);
        final EditText editText = (EditText) findViewById(editTextId);
        if (dateOrTime instanceof LocalDate) {
            editText.setText(((LocalDate) dateOrTime).toString(MeasurementData.DATE_PATTERN));
            editText.setOnClickListener(new DateListener(editText));
        } else if (dateOrTime instanceof LocalTime) {
            editText.setText(((LocalTime) dateOrTime).toString(MeasurementData.TIME_PATTERN));
            editText.setOnClickListener(new TimeListener(editText));
        }
    }

    private void formatWeekdayCheckBox(int checkBoxId, int weekdayId) {
        final CheckBox checkBox = (CheckBox) findViewById(checkBoxId);
        checkBox.setChecked(DataProvider.getActiveWeekdays().contains(weekdayId));
    }

    public void onShowAllWeekdays(View view) {
        final CheckBox checkBox = (CheckBox) view;
        if (checkBox.isChecked()) {
            DataProvider.getActiveWeekdays().addAll(Arrays.asList(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                    DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY,
                    DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY,
                    DateTimeConstants.SUNDAY));
            for (int i = 0; i < weekdayArray.size(); i++) {
                formatWeekdayCheckBox(weekdayArray.keyAt(i), weekdayArray.valueAt(i));
            }
        }
    }

    public void onWeekday(View view) {
        if (!((CheckBox) view).isChecked()) {
            DataProvider.getActiveWeekdays().remove(weekdayArray.get(view.getId()));
            ((CheckBox) findViewById(R.id.filter_show_all_weekdays)).setChecked(false);
        } else {
            DataProvider.getActiveWeekdays().add(weekdayArray.get(view.getId()));

            final CheckBox checkBoxAllWeekdays = (CheckBox) findViewById(R.id.filter_show_all_weekdays);
            checkBoxAllWeekdays.setChecked(7 == DataProvider.getActiveWeekdays().size());
        }
    }

    @Override
    public void finish() {
        DataProvider.setStartDate(getDateResult(R.id.filter_data_start_date_check, R.id.filter_data_start_date));
        DataProvider.setEndDate(getDateResult(R.id.filter_data_end_date_check, R.id.filter_data_end_date));
        DataProvider.setStartTime(getTimeResult(R.id.filter_data_start_time_check, R.id.filter_data_start_time));
        DataProvider.setEndTime(getTimeResult(R.id.filter_data_end_time_check, R.id.filter_data_end_time));

        DataProvider.getDataProvider().updateFilter();
        super.finish();
    }

    private LocalDate getDateResult(int checkBoxId, int editBoxId) {
        if (((CheckBox) findViewById(checkBoxId)).isChecked()) {
            return LocalDate.parse(((EditText) findViewById(editBoxId)).getText().toString());
        } else {
            return null;
        }
    }

    private LocalTime getTimeResult(int checkBoxId, int editBoxId) {
        if (((CheckBox) findViewById(checkBoxId)).isChecked()) {
            return LocalTime.parse(((EditText) findViewById(editBoxId)).getText().toString());
        } else {
            return null;
        }
    }

    private class DateListener implements View.OnClickListener {

        private final EditText editDate;

        DateListener(EditText editDate) {
            this.editDate = editDate;
        }

        @Override
        public void onClick(final View view) {
            String[] date = editDate.getText().toString().split("-");

            int year = Integer.valueOf(date[0]);
            int month = Integer.valueOf(date[1]) - 1;
            int day = Integer.valueOf(date[2]);

            DatePickerDialog datePicker;
            datePicker = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                    editDate.setText(view.getResources().getString(R.string.edit_data_date_format, selectedYear, selectedMonth + 1, selectedDay));
                }
            }, year, month, day);
            datePicker.setTitle(R.string.edit_data_date_picker_title);
            datePicker.show();
        }
    }

    private class TimeListener implements View.OnClickListener {

        private final EditText editTime;

        TimeListener(EditText editTime) {
            this.editTime = editTime;
        }

        @Override
        public void onClick(final View view) {
            String[] time = editTime.getText().toString().split(":");

            int hour = Integer.valueOf(time[0]);
            int minute = Integer.valueOf(time[1]);

            TimePickerDialog timePicker;
            timePicker = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    editTime.setText(view.getResources().getString(R.string.edit_data_time_format, hour, minute));
                }
            }, hour, minute, true);
            timePicker.setTitle(R.string.edit_data_time_picker_title);
            timePicker.show();
        }
    }
}
