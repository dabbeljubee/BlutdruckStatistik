package de.dabbeljubee.blutdruckstatistik;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.print.PrintManager;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementData;
import de.dabbeljubee.blutdruckstatistik.Tools.PrintReportAdapter;

import java.util.logging.Logger;

import static de.dabbeljubee.blutdruckstatistik.Logic.DataProvider.*;

public class MainActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger("MainActivity");
    private static final int IMPORT_CSV_CODE = 1;
    private static final int IMPORT_JSON_CODE = 2;
    private static final int INPUT_ROLLOVER_VALUE = 35;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        getDataProvider().updatePreferences(getApplicationContext());
        getDataProvider().readFromFile(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent actionIntent = new Intent(this, SettingsActivity.class);
                startActivity(actionIntent);
                return true;

            case R.id.filter_settings:
                Intent filterIntent = new Intent(this, FilterDataActivity.class);
                startActivity(filterIntent);
                return true;

            case R.id.action_export_csv:
                LOGGER.fine("Save data as csv to file");
                if (isStoragePermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    getDataProvider().exportAsCsv(getApplicationContext());
                }
                return true;

            case R.id.action_export_json:
                LOGGER.fine("Save data as json to file");
                if (isStoragePermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    getDataProvider().exportAsJson(getApplicationContext());
                }
                return true;

            case R.id.action_import_csv:
                LOGGER.fine("Import data from file");
                if (isStoragePermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    return importData(IMPORT_CSV_CODE);
                }

            case R.id.action_import_json:
                LOGGER.fine("Import data from file");
                if (isStoragePermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    return importData(IMPORT_JSON_CODE);
                }

            case R.id.action_print_report:
                printStatisticsReport();
                return true;

            case R.id.action_help:
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://dabbeljubee.de/Blutdruck-Statistik")));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isStoragePermissionGranted(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                LOGGER.fine("Permission is granted");
                return true;
            } else {

                LOGGER.fine("Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            LOGGER.fine("Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            LOGGER.fine("Permission: " + permissions[0] + " was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private boolean importData(int importCsvCode) {
        Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        importIntent.addCategory(Intent.CATEGORY_OPENABLE);
        importIntent.setType("*/*");

        startActivityForResult(importIntent, importCsvCode);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                LOGGER.info(uri.toString());
//                String fileName = uri.getLastPathSegment().substring(uri.getLastPathSegment().indexOf(":") + 1);
                String fileName = getFileName(uri);
                LOGGER.info("Import data from file: " + fileName);
                LOGGER.info(getFileName(uri));
                LOGGER.info(uri.toString());
                if (IMPORT_CSV_CODE == requestCode) {
                    getDataProvider().importCsv(getApplicationContext(), fileName);
                }
                if (IMPORT_JSON_CODE == requestCode) {
                    getDataProvider().importJson(getApplicationContext(), fileName);
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static EnumFocusView inputFocus;
    private static MeasurementListAdapter measurementListAdapter = null;
    private static MeasurementStatisticAdapter measurementStatisticAdapter = null;

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final Logger LOGGER = Logger.getLogger("PlaceholderFragment");

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_input, container, false);
                    inputFocus = EnumFocusView.SYSTOLIC;

                    ImageButton saveButton = (ImageButton) rootView.findViewById(R.id.buttonSave);
                    setImageButtonEnabled(rootView.getContext(), false, saveButton);
                    break;

                case 2:
                    rootView = inflater.inflate(R.layout.fragment_list, container, false);

                    ListView listView = (ListView) rootView.findViewById(R.id.data_list);
                    listView.setOnItemClickListener(new ListViewClickListener());
                    if (null == measurementListAdapter) {
                        measurementListAdapter = new MeasurementListAdapter(
                                getContext(),
                                R.layout.list_row,
                                getDataProvider().getDataDescending());
                    }
                    listView.setAdapter(measurementListAdapter);

                    break;

                case 3:
                    LOGGER.fine("Build statistic view");
                    rootView = inflater.inflate(R.layout.fragment_statistic, container, false);

                    Spinner spinner = (Spinner) rootView.findViewById(R.id.statistic_spinner);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            rootView.getContext(), R.array.statistic_spinner_choices, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            ExpandableListView statisticView = (ExpandableListView) rootView.findViewById(R.id.statistic_data);
                            switch (position) {
                                case 1:
                                    measurementStatisticAdapter = new MeasurementStatisticSystolicAdapter();
                                    break;
                                case 2:
                                    measurementStatisticAdapter = new MeasurementStatisticDiastolicAdapter();
                                    break;
                                case 3:
                                    measurementStatisticAdapter = new MeasurementStatisticPulseAdapter();
                                    break;
                                default:
                                    measurementStatisticAdapter = new MeasurementStatisticDiaSysAdapter();
                                    break;

                            }
                            statisticView.setAdapter(measurementStatisticAdapter);
                            measurementStatisticAdapter.formatHeader(rootView);
                            MainActivity.triggerViewUpdate(false);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    ExpandableListView statisticView = (ExpandableListView) rootView.findViewById(R.id.statistic_data);
                    if (null == measurementStatisticAdapter) {
                        measurementStatisticAdapter = new MeasurementStatisticDiaSysAdapter();
                    }
                    statisticView.setAdapter(measurementStatisticAdapter);

                    measurementStatisticAdapter.formatHeader(rootView);

                    break;

                default:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    TextView textView = (TextView) rootView.findViewById(R.id.section_label);
                    textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
                    break;
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    public void onButtonNumeric(View view) {
        String valueToAdd = ((TextView)view).getText().toString();
        TextView focusView = inputFocus.getFocusView(view);
        String value = appendValue(valueToAdd, focusView.getText().toString());
        focusView.setText(value);

        if (INPUT_ROLLOVER_VALUE < Integer.valueOf(value)) {
            switchToNextValue(view);
        }
    }

    String appendValue(String valueToAdd, String value) {


        if (null == value)
            value = "0";
        if (null == valueToAdd)
            valueToAdd = "0";
        if (value.isEmpty() || value.equals("0") || INPUT_ROLLOVER_VALUE < Integer.valueOf(value)) {
            value = valueToAdd;
        } else {
            value = value + valueToAdd;
        }
        return value;
    }

    public void onButtonNext(View view) {
        switchToNextValue(view);
    }

    public void onButtonClear(View view) {
        TextView focusView = inputFocus.getFocusView(view);
        focusView.setText("0");
        enableSaveButtonIfPossible(view);
    }

    public void onButtonSave(View view) {
        MeasurementData measurementData = saveMeasurementData(view);
        triggerViewUpdate(false);
        displayToastForUpdate(view, measurementData);
    }

    private MeasurementData saveMeasurementData(View view) {
        TextView systolicValue = (TextView) view.getRootView().findViewById(R.id.systolicValue);
        TextView diastolicValue = (TextView) view.getRootView().findViewById(R.id.diastolicValue);
        TextView pulseValue = (TextView) view.getRootView().findViewById(R.id.pulseValue);
        MeasurementData measurementData = saveMeasurementData(
                Integer.valueOf(systolicValue.getText().toString()),
                Integer.valueOf(diastolicValue.getText().toString()),
                Integer.valueOf(pulseValue.getText().toString()));
        getDataProvider().persistAsJson(view.getContext());
        systolicValue.setText("0");
        diastolicValue.setText("0");
        pulseValue.setText("0");
        enableSaveButtonIfPossible(view);
        return measurementData;
    }

    private void displayToastForUpdate(View view, MeasurementData measurementData) {
        String message = String.format(view.getResources().getString(R.string.input_toast_save_text),
                measurementData.getSystolicValue(),
                measurementData.getDiastolicValue(),
                measurementData.getPulse(),
                measurementData.formatDateTimeAsString());
        Toast toast = Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

    MeasurementData saveMeasurementData(Integer systolicValue, Integer diastolicValue, Integer pulse) {
        MeasurementData measurementData = new MeasurementData(systolicValue, diastolicValue, pulse);
        getDataProvider().addToData(measurementData);
        return measurementData;
    }

    private void switchToNextValue(View view) {
        TextView focusView = inputFocus.getFocusView(view);
        Drawable border = focusView.getBackground();
        focusView.setBackground(null);
        switch (inputFocus) {
            case SYSTOLIC:
                inputFocus = EnumFocusView.DIASTOLIC;
                break;
            case DIASTOLIC:
                inputFocus = EnumFocusView.PULSE;
                break;
            case PULSE:
                inputFocus = EnumFocusView.SYSTOLIC;
                break;
        }
        inputFocus.getFocusView(view).setBackground(border);
        enableSaveButtonIfPossible(view);
    }

    private void enableSaveButtonIfPossible(View view) {
        ImageButton saveButton = (ImageButton) view.getRootView().findViewById(R.id.buttonSave);
        if (0 < Integer.valueOf(((TextView) view.getRootView().findViewById(R.id.systolicValue)).getText().toString())
                && 0 < Integer.valueOf(((TextView) view.getRootView().findViewById(R.id.diastolicValue)).getText().toString())
                && 0 < Integer.valueOf(((TextView) view.getRootView().findViewById(R.id.pulseValue)).getText().toString())) {

            saveButton.setClickable(true);
            saveButton.setEnabled(true);
            setImageButtonEnabled(view.getContext(), true, saveButton);
        } else {
            saveButton.setClickable(false);
            saveButton.setEnabled(false);
            setImageButtonEnabled(view.getContext(), false, saveButton);
        }
        view.getRootView().invalidate();
    }

    static void setImageButtonEnabled(Context ctxt, boolean enabled, ImageButton item) {

        item.setClickable(enabled);
        item.setEnabled(enabled);
        Drawable originalIcon = ResourcesCompat.getDrawable(ctxt.getResources(), android.R.drawable.ic_menu_save, null);
        item.setImageDrawable(enabled ? originalIcon : convertDrawableToGrayScale(originalIcon));
    }

    private static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null)
            return null;

        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    public static void triggerViewUpdate(boolean preferencesChanged) {
        if (null != measurementStatisticAdapter) {
            measurementStatisticAdapter.update(preferencesChanged);
            measurementStatisticAdapter.notifyDataSetChanged();
        }
        if (null != measurementListAdapter) {
            measurementListAdapter.clear();
            measurementListAdapter.addAll(getDataProvider().getDataDescending());
            measurementListAdapter.notifyDataSetChanged();
        }
    }
    private void printStatisticsReport() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        String jobName = getString(R.string.print_job_statistics);

        printManager.print(jobName, new PrintReportAdapter(this), null);
    }
}
