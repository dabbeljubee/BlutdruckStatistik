package de.dabbeljubee.blutdruckstatistik;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementData;
import org.joda.time.DateTime;

import java.util.Locale;
import java.util.logging.Logger;

public class EditDataActivity extends AppCompatActivity {

    private  final static Logger LOGGER = Logger.getLogger("EditDataActivity");
    private MeasurementData measurementData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data);

        final long datetime = getIntent().getLongExtra("datetime", 0);
        LOGGER.fine(String.format(Locale.GERMAN, "Data: %d", datetime));

        measurementData = DataProvider.getDataProvider().getFilteredMeasurementsMap().get(datetime);

        final EditText editDate = (EditText) findViewById(R.id.edit_data_date);
        editDate.setText(measurementData.getDateTime().toLocalDate().toString(MeasurementData.DATE_PATTERN));
        editDate.setOnClickListener(new DateListener(editDate));

        final EditText editTime = (EditText) findViewById(R.id.edit_data_time);
        editTime.setText(measurementData.getDateTime().toLocalTime().toString(MeasurementData.TIME_PATTERN));
        editTime.setOnClickListener(new TimeListener(editTime));

        final NumberPicker sysPicker = (NumberPicker) findViewById(R.id.edit_data_sys_editor);
        sysPicker.setMinValue(0);
        sysPicker.setMaxValue(500);
        sysPicker.setValue(measurementData.getSystolicValue());
        final NumberPicker diaPicker = (NumberPicker) findViewById(R.id.edit_data_dia_editor);
        diaPicker.setMinValue(0);
        diaPicker.setMaxValue(500);
        diaPicker.setValue(measurementData.getDiastolicValue());
        final NumberPicker pulsePicker = (NumberPicker) findViewById(R.id.edit_data_pulse_editor);
        pulsePicker.setMinValue(0);
        pulsePicker.setMaxValue(500);
        pulsePicker.setValue(measurementData.getPulse());
        final EditText commentEditor = (EditText) findViewById(R.id.edit_data_comment_editor);
        commentEditor.setText(String.valueOf(null == measurementData.getComment() ? "" : measurementData.getComment()));
    }

    public void onEditActivityButtonSave(View view) {
        final EditText editDate = (EditText) findViewById(R.id.edit_data_date);
        String date = editDate.getText().toString();
        final EditText editTime = (EditText) findViewById(R.id.edit_data_time);
        String time = editTime.getText().toString();

        final NumberPicker sysPicker = (NumberPicker) findViewById(R.id.edit_data_sys_editor);
        int sysValue = sysPicker.getValue();
        final NumberPicker diaPicker = (NumberPicker) findViewById(R.id.edit_data_dia_editor);
        int diaValue = diaPicker.getValue();
        final NumberPicker pulsePicker = (NumberPicker) findViewById(R.id.edit_data_pulse_editor);
        int pulseValue = pulsePicker.getValue();
        final EditText commentEditor = (EditText) findViewById(R.id.edit_data_comment_editor);
        String comment = commentEditor.getText().toString();

        MeasurementData changedMeasurementData = new MeasurementData(DateTime.parse(date + "T" + time), sysValue, diaValue, pulseValue, false, comment);

        if (changedMeasurementData.getDateTime().isEqual(measurementData.getDateTime())) {
            DataProvider.getDataProvider().removeFromData(measurementData);
        }
        DataProvider.getDataProvider().addToData(changedMeasurementData);
        DataProvider.getDataProvider().persistAsJson(view.getContext());
        MainActivity.triggerViewUpdate(false);
        finish();
    }

    public void onButtonCancel(View view) {
        finish();
    }

    public void onButtonDelete(final View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());

        alertDialogBuilder.setTitle(R.string.edit_data_delete_confirmation);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.ACK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DataProvider.getDataProvider().removeFromData(measurementData);
                        DataProvider.getDataProvider().persistAsJson(view.getContext());
                        MainActivity.triggerViewUpdate(false);
                        finish();
                    }})
                .setNegativeButton(R.string.NACK,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }});
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
