package de.dabbeljubee.blutdruckstatistik.Logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.dabbeljubee.blutdruckstatistik.R;
import de.dabbeljubee.blutdruckstatistik.Tools.FileAccess;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.dabbeljubee.blutdruckstatistik.MainActivity;
import de.dabbeljubee.blutdruckstatistik.Tools.DateTimeGsonConverter;
import org.joda.time.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class DataProvider {

    private static final Logger LOGGER = Logger.getLogger("DataProvider");

    public static class MeasurementsDataFormat extends TreeMap<Long, MeasurementData> {}
    private static class MeasurementsFileFormat extends ArrayList<MeasurementData> {}

    private static final String INTERNAL_FILE_NAME = "owner_data";

    private static final GsonBuilder GSON_BUILDER =
            new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeGsonConverter()).setPrettyPrinting();
    private static final GsonBuilder GSON_BUILDER_EXPORT =
            new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeGsonConverter())
            .setExclusionStrategies(new ExportDataExclusionStrategy())
            .setPrettyPrinting();

    private static final FileAccess FILE_ACCESS = new FileAccess();
    private static final DataProvider DATA_PROVIDER = new DataProvider();

    private static SharedPreferences preferences;

    private final MeasurementsDataFormat measurementsMap = new MeasurementsDataFormat();
    private final MeasurementsDataFormat filteredMeasurementsMap = new MeasurementsDataFormat();

    private final TreeMap<Long, MeasurementStatisticMonth> monthsMap = new TreeMap<>();
    private final TreeMap<Long, MeasurementStatisticWeek> weeksMap = new TreeMap<>();

    private static LocalDate startDate = null;
    private static LocalDate endDate = null;
    private static LocalTime startTime = null;
    private static LocalTime endTime = null;
    private static final Set<Integer> activeWeekdays = new HashSet<>(Arrays.asList(
                    DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
                    DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY,
                    DateTimeConstants.FRIDAY, DateTimeConstants.SATURDAY,
                    DateTimeConstants.SUNDAY));
    public static final MeasurementLevels sysLevels = new MeasurementLevels(139, 125, 100);
    public static final MeasurementLevels diaLevels = new MeasurementLevels(95, 85, 70);
    public static final MeasurementLevels pulseLevels = new MeasurementLevels(90, 70, 50);

    public static DataProvider getDataProvider() {
        return DATA_PROVIDER;
    }

    public MeasurementsDataFormat getFilteredMeasurementsMap() {
        return filteredMeasurementsMap;
    }

    public TreeMap<Long, MeasurementStatisticMonth> getMonthsMap() {
        return monthsMap;
    }

    TreeMap<Long, MeasurementStatisticWeek> getWeeksMap() {
        return weeksMap;
    }
    public void addToData(MeasurementData measurementData) {
        DataProvider.getDataProvider().measurementsMap.put(measurementData.getDateTime().getMillis(), measurementData);

        if (matchesFilter(measurementData)) {
            DataProvider.getDataProvider().filteredMeasurementsMap.put(measurementData.getDateTime().getMillis(), measurementData);
        }

        DataProvider.addToStatistics(measurementData);
    }

    public void removeFromData(MeasurementData measurementData) {
        DataProvider.getDataProvider().measurementsMap.remove(measurementData.getDateTime().getMillis());

        if (matchesFilter(measurementData)) {
            LOGGER.fine("Remove data");
            DataProvider.getDataProvider().filteredMeasurementsMap.remove(measurementData.getDateTime().getMillis());
            DataProvider.removeFromStatistics(measurementData.getDateTime(), measurementData.getSystolicValue(), measurementData.getDiastolicValue(), measurementData.getPulse());
        }
    }

    private static boolean matchesFilter(MeasurementData dataItem) {
        boolean matches = true;

        if (null != startDate && null != endDate) {
            if (startDate.isAfter(endDate)) {
                if (startDate.isAfter(dataItem.getDateTime().toLocalDate())
                        && endDate.isBefore(dataItem.getDateTime().toLocalDate())) {
                    matches = false;
                }
            } else {
                if (startDate.isAfter(dataItem.getDateTime().toLocalDate())
                        || endDate.isBefore(dataItem.getDateTime().toLocalDate())) {
                    matches = false;
                }
            }
        } else {
            if (!((null == startDate) || startDate.isAfter(dataItem.getDateTime().toLocalDate()))
                    || !((null == endDate) || endDate.isBefore(dataItem.getDateTime().toLocalDate()))) {
                matches = false;
            }
        }

        if (null != startTime && null != endTime) {
            if (startTime.isAfter(endTime)) {
                if (startTime.isAfter(dataItem.getDateTime().toLocalTime())
                        && endTime.isBefore(dataItem.getDateTime().toLocalTime())) {
                    matches = false;
                }
            } else {
                if (startTime.isAfter(dataItem.getDateTime().toLocalTime())
                        || endTime.isBefore(dataItem.getDateTime().toLocalTime())) {
                    matches = false;
                }
            }
        } else {
            if (!((null == startTime) || startTime.isAfter(dataItem.getDateTime().toLocalTime()))
                    || !((null == endTime) || endTime.isBefore(dataItem.getDateTime().toLocalTime()))) {
                matches = false;
            }
        }

        if (7 > activeWeekdays.size() && !activeWeekdays.contains(dataItem.getDateTime().getDayOfWeek())) {
            matches = false;
        }

        return matches;
//        return !((null != startDate) && startDate.isAfter(dataItem.getDateTime().toLocalDate()))
//                && !((null != endDate) && endDate.isBefore(dataItem.getDateTime().toLocalDate()))
//                && !((null != startTime) && startTime.isAfter(dataItem.getDateTime().toLocalTime()))
//                && !((null != endTime) && endTime.isBefore(dataItem.getDateTime().toLocalTime()))
//                && (7 == activeWeekdays.size() || activeWeekdays.contains(dataItem.getDateTime().getDayOfWeek()));
    }

    public void readFromFile(Context context) {
        try {
            StringBuffer fileContent = FILE_ACCESS.readFromInternalFile(context, INTERNAL_FILE_NAME);

            MeasurementsFileFormat fileData = new DataProvider.MeasurementsFileFormat();

            Gson gson = GSON_BUILDER.create();
            fileData.addAll(gson.fromJson(fileContent.toString(), fileData.getClass()));

            for (MeasurementData dataItem : fileData) {
                addToData(dataItem);
            }
        } catch (FileNotFoundException e) {
            LOGGER.info("Input file not found");
        } catch (IOException e) {
            LOGGER.severe("Error reading file");
        }

    }

    public void importJson(Context context, String fileName) {
        try {
            StringBuffer fileContent = FILE_ACCESS.readFromExternalFile(fileName);

            MeasurementsFileFormat fileData = new DataProvider.MeasurementsFileFormat();

            Gson gson = GSON_BUILDER.create();
            fileData.addAll(gson.fromJson(fileContent.toString(), fileData.getClass()));

            for (MeasurementData dataItem : fileData) {
                addToData(dataItem);
            }

            persistAsJson(context);
            MainActivity.triggerViewUpdate(false);
        } catch (FileNotFoundException e) {
            LOGGER.info("Input file not found");
        } catch (IOException e) {
            LOGGER.severe("Error reading file");
        }
    }

    public void importCsv(Context context, String fileName) {
        try {
            List<String> lines = FILE_ACCESS.readFromExternalFileByLine(fileName);

            if(!"dateTime,systolicValue,diastolicValue,pulse,comment".equals(lines.get(0))) {
                LOGGER.info(lines.get(0));
                Toast.makeText(context, R.string.action_import_csv_invalid_header, Toast.LENGTH_SHORT).show();
                return;
            }

            List<MeasurementData> measurementDataList = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                try {
                    LOGGER.info(lines.get(i));
                    StringTokenizer tokenizer = new StringTokenizer(lines.get(i), ",");
                    DateTime dateTime = DateTime.parse(tokenizer.nextToken());
                    Integer sysValue = Integer.valueOf(tokenizer.nextToken());
                    Integer diaValue = Integer.valueOf(tokenizer.nextToken());
                    Integer pulseValue = Integer.valueOf(tokenizer.nextToken());
                    StringBuilder comment = new StringBuilder();
                    while (tokenizer.hasMoreTokens()) {
                        comment.append(tokenizer.nextToken());
                        if (tokenizer.hasMoreTokens()) {
                            comment.append(",");
                        }
                    }
                    measurementDataList.add(new MeasurementData(dateTime, sysValue, diaValue, pulseValue, false, comment.toString()));
                } catch (Exception e) {
                    LOGGER.info(lines.get(i));
                    LOGGER.info(e.getMessage());
                    Toast.makeText(context, context.getResources().getString(R.string.action_import_csv_invalid_data, i), Toast.LENGTH_LONG).show();
                    return;
                }
            }

            for (MeasurementData dataItem : measurementDataList) {
                addToData(dataItem);
            }

            persistAsJson(context);
            MainActivity.triggerViewUpdate(false);
        } catch (FileNotFoundException e) {
            LOGGER.info("Input file not found");
        } catch (IOException e) {
            LOGGER.severe("Error reading file");
        }
    }

    public void persistAsJson(Context context) {

        Gson gson = GSON_BUILDER.setPrettyPrinting().create();
        String json = gson.toJson(new ArrayList<>(measurementsMap.values()));
        LOGGER.fine(json);

        saveToInternalFile(context, INTERNAL_FILE_NAME, json);
    }

    public void exportAsJson(Context context) {

        Gson gson = GSON_BUILDER_EXPORT.setExclusionStrategies(new ExportDataExclusionStrategy()).create();
        String json = gson.toJson(new ArrayList<>(measurementsMap.values()));
        LOGGER.fine(json);

        saveToExternalFile(context, context.getResources().getString(R.string.json_name, LocalDate.now().toString("yyyyMMdd")), json);
    }

    public void exportAsCsv(Context context) {

        StringBuilder csv = new StringBuilder("dateTime,systolicValue,diastolicValue,pulse,comment\n");

        for (MeasurementData measurementData : measurementsMap.values()) {
            csv.append(measurementData.getDateTime().toString(MeasurementData.DATE_TIME_PATTERN).replace(' ', 'T')).append(',')
                    .append(measurementData.getSystolicValue()).append(',')
                    .append(measurementData.getDiastolicValue()).append(',')
                    .append(measurementData.getPulse()).append(',')
                    .append(measurementData.getCommentNotNull()).append("\n");
        }

        LOGGER.fine(csv.toString());

        saveToExternalFile(context, context.getResources().getString(R.string.csv_name, LocalDate.now().toString("yyyyMMdd")), csv.toString());
    }

    private void saveToExternalFile(Context context, String fileName, String content) {
        FILE_ACCESS.saveToExternalFile(context, fileName, content);
    }

    private void saveToInternalFile(Context context, String fileName, String content) {
        FILE_ACCESS.saveToInternalFile(context, fileName, content);
    }

    public void updatePreferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        updateLevels(context);
        reloadStatistics();
        MainActivity.triggerViewUpdate(true);
    }

    public void updateFilter() {
        filteredMeasurementsMap.clear();
        for (MeasurementData measurementData : measurementsMap.values()) {
            if (matchesFilter(measurementData)) {
                filteredMeasurementsMap.put(measurementData.getDateTime().getMillis(), measurementData);
            }
        }

        reloadStatistics();
        MainActivity.triggerViewUpdate(false);
    }

    private void updateLevels(Context context) {
        sysLevels.setAlarm(getValueFromPreferencesAsInt(context, R.string.settings_sys_alarm, "139"));
        sysLevels.setWarning(getValueFromPreferencesAsInt(context, R.string.settings_sys_warning, "125"));
        sysLevels.setLow(getValueFromPreferencesAsInt(context, R.string.settings_sys_low, "100"));

        diaLevels.setAlarm(getValueFromPreferencesAsInt(context, R.string.settings_dia_alarm, "95"));
        diaLevels.setWarning(getValueFromPreferencesAsInt(context, R.string.settings_dia_warning, "85"));
        diaLevels.setLow(getValueFromPreferencesAsInt(context, R.string.settings_dia_low, "70"));

        pulseLevels.setAlarm(getValueFromPreferencesAsInt(context, R.string.settings_pulse_alarm, "90"));
        pulseLevels.setWarning(getValueFromPreferencesAsInt(context, R.string.settings_pulse_warning, "70"));
        pulseLevels.setLow(getValueFromPreferencesAsInt(context, R.string.settings_pulse_low, "50"));
    }

    private void reloadStatistics()  {
        getWeeksMap().clear();
        getMonthsMap().clear();

        for (MeasurementData measurementData : measurementsMap.values()) {
            addToStatistics(measurementData);
        }
    }

    private Integer getValueFromPreferencesAsInt(Context context, int valueId, String defValue) {
        return Integer.valueOf(preferences.getString(context.getResources().getString(valueId), defValue));
    }

    public List<MeasurementData> getDataDescending() {
        return new ArrayList<>(filteredMeasurementsMap.descendingMap().values());
    }

    public List<MeasurementStatisticMonth> getMonthsAscending() {
        return new ArrayList<>(getMonthsMap().values());
    }

    public List<MeasurementStatisticWeek> getWeeksOfMonthAscending(Long monthKey) {
        return new ArrayList<>(getMonthsMap().get(monthKey).getRelatedWeeks().values());
    }

    public List<MeasurementStatisticMonth> getMonthsDescending() {
        return new ArrayList<>(getMonthsMap().descendingMap().values());
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    static void addToStatistics(MeasurementData measurementData) {
        if (matchesFilter(measurementData)) {
            findRelatedMonth(measurementData.getDateTime()).addItem(measurementData.getSystolicValue(), measurementData.getDiastolicValue(), measurementData.getPulse());
            findRelatedWeek(measurementData.getDateTime()).addItem(measurementData.getSystolicValue(), measurementData.getDiastolicValue(), measurementData.getPulse());
        }
    }

    private static void removeFromStatistics(DateTime dateTime, int sysValue, int diaValue, int pulseValue) {
        final MeasurementStatisticMonth relatedMonth = findRelatedMonth(dateTime);
        relatedMonth.removeItem(sysValue, diaValue, pulseValue);
        final MeasurementStatisticWeek relatedWeek = findRelatedWeek(dateTime);
        relatedWeek.removeItem(sysValue, diaValue, pulseValue);
        if(0 == relatedWeek.getDiastolic().getValueCount().intValue()) {
            getDataProvider().getWeeksMap().remove(relatedWeek.getFirstDayAsLong());
            for (MeasurementStatisticMonth statisticMonth: getDataProvider().getMonthsMap().values()) {
                if (statisticMonth.getRelatedWeeks().containsKey(relatedWeek.getFirstDayAsLong())) {
                    statisticMonth.getRelatedWeeks().remove(relatedWeek.getFirstDayAsLong());
                }
            }
        }
        if (0 == relatedMonth.getSystolic().getValueCount().intValue()
                && relatedMonth.getRelatedWeeks().isEmpty()) {
            getDataProvider().getMonthsMap().remove(relatedMonth.getFirstDayAsLong());
        }
    }

    private static MeasurementStatisticMonth findRelatedMonth(DateTime dateTime) {
        DateTime firstDayOfMonth = dateTime.withDayOfMonth(1).withZoneRetainFields(DateTimeZone.getDefault()).withTimeAtStartOfDay();
        MeasurementStatisticMonth month = getDataProvider().getMonthsMap().get(firstDayOfMonth.getMillis());
        if (null == month) {
            month = new MeasurementStatisticMonth(firstDayOfMonth);
            getDataProvider().getMonthsMap().put(firstDayOfMonth.getMillis(), month);
            LOGGER.fine(String.format("Add Month  %s", firstDayOfMonth.toString()));
        }
        LOGGER.fine(String.format("Month %s; %s", month.getFirstDayAsString(), month.toString()));
        return month;
    }

    private static MeasurementStatisticWeek findRelatedWeek(DateTime dateTime) {
        DateTime firstDayOfWeek = dateTime.withDayOfWeek(1).withZoneRetainFields(DateTimeZone.getDefault()).withTimeAtStartOfDay();
        MeasurementStatisticWeek week = getDataProvider().getWeeksMap().get(firstDayOfWeek.getMillis());
        if (null == week) {
            week = new MeasurementStatisticWeek(firstDayOfWeek);
            getDataProvider().getWeeksMap().put(firstDayOfWeek.getMillis(), week);
        }

        LOGGER.fine(String.format(Locale.GERMAN, "NoOfWeeks %d", getDataProvider().getWeeksMap().size()));
        LOGGER.fine(String.format(Locale.GERMAN, "Week %s; %s; %s", week.getFirstDayAsString(), week.toString(), week.getFirstDay().toString()));

        return week;
    }

    public static class ExportDataExclusionStrategy implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return ( MeasurementData.class == fieldAttributes.getDeclaringClass() && fieldAttributes.getName().equals("saved"));
        }

    }

    public static LocalDate getStartDate() {
        return startDate;
    }

    public static void setStartDate(LocalDate startDate) {
        DataProvider.startDate = startDate;
    }

    public static LocalDate getEndDate() {
        return endDate;
    }

    public static void setEndDate(LocalDate endDate) {
        DataProvider.endDate = endDate;
    }

    public static LocalTime getStartTime() {
        return startTime;
    }

    public static void setStartTime(LocalTime startTime) {
        DataProvider.startTime = startTime;
    }

    public static LocalTime getEndTime() {
        return endTime;
    }

    public static void setEndTime(LocalTime endTime) {
        DataProvider.endTime = endTime;
    }

    public static Set<Integer> getActiveWeekdays() {
        return activeWeekdays;
    }
}
