package de.dabbeljubee.blutdruckstatistik.Logic;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DataProviderTest {

    private final DataProvider dataProvider = DataProvider.getDataProvider();

    @Before
    public  void before() {
        dataProvider.getWeeksMap().clear();
        dataProvider.getMonthsMap().clear();
    }

    @Test
    public void testWithEmptyData() {
        DateTime dateTime = DateTime.parse("2017-01-17T12:00:00.000+01:00");
        Long weekKey = dateTime.withDayOfWeek(1).withMillisOfDay(0).getMillis();
        Long monthKey = dateTime.withDayOfMonth(1).withMillisOfDay(0).getMillis();

        DataProvider.addToStatistics(new MeasurementData(dateTime, 100, 80, 60, false, null));

        assertThat(dataProvider.getWeeksMap().size(), is(1));
        assertNotNull(dataProvider.getWeeksMap().get(weekKey));
        MeasurementStatisticWeek week = dataProvider.getWeeksMap().get(weekKey);

        assertThat(dataProvider.getMonthsMap().size(), is(1));
        assertNotNull(dataProvider.getMonthsMap().get(monthKey));
        MeasurementStatisticMonth month = dataProvider.getMonthsMap().get(monthKey);
        assertThat(month.getRelatedWeeks().size(), is(1));
        assertNotNull(month.getRelatedWeeks().get(weekKey));

        assertThat(week, is(month.getRelatedWeeks().get(weekKey)));

        assertThat(week.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(month.getSystolic().getValueCount(), is(BigDecimal.ONE));
    }

    @Test
    public void testWithEmptyDataWhereWeekBelongToNextMonth() {
        DateTime dateTime = DateTime.parse("2017-01-31T12:00:00.000+01:00");
        Long weekKey = dateTime.withDayOfWeek(1).withMillisOfDay(0).getMillis();
        Long monthKey = dateTime.withDayOfMonth(1).withMillisOfDay(0).getMillis();
        Long nextMonthKey = new DateTime(monthKey).plusMonths(1).getMillis();

        DataProvider.addToStatistics(new MeasurementData(dateTime, 100, 80, 60, false, null));

        assertThat(dataProvider.getWeeksMap().size(), is(1));
        assertNotNull(dataProvider.getWeeksMap().get(weekKey));
        MeasurementStatisticWeek week = dataProvider.getWeeksMap().get(weekKey);

        assertThat(dataProvider.getMonthsMap().size(), is(2));
        assertNotNull(dataProvider.getMonthsMap().get(monthKey));
        MeasurementStatisticMonth month = dataProvider.getMonthsMap().get(monthKey);
        assertThat(month.getRelatedWeeks().size(), is(0));
        MeasurementStatisticMonth nextMonth = dataProvider.getMonthsMap().get(nextMonthKey);
        assertThat(nextMonth.getRelatedWeeks().size(), is(1));
        assertNotNull(nextMonth.getRelatedWeeks().get(weekKey));

        assertThat(week, is(nextMonth.getRelatedWeeks().get(weekKey)));

        assertThat(week.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(month.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(nextMonth.getSystolic().getValueCount(), is(BigDecimal.ZERO));
    }

    @Test
    public void testWithEmptyDataWhereWeekBelongToPreviousMonth() {
        DateTime dateTime = DateTime.parse("2017-01-01T12:00:00.000+01:00");
        Long weekKey = dateTime.withDayOfWeek(1).withMillisOfDay(0).getMillis();
        Long monthKey = dateTime.withDayOfMonth(1).withMillisOfDay(0).getMillis();
        Long previousMonthKey = new DateTime(monthKey).minusMonths(1).getMillis();

        DataProvider.addToStatistics(new MeasurementData(dateTime, 100, 80, 60, false, null));

        assertThat(dataProvider.getWeeksMap().size(), is(1));
        assertNotNull(dataProvider.getWeeksMap().get(weekKey));
        MeasurementStatisticWeek week = dataProvider.getWeeksMap().get(weekKey);

        assertThat(dataProvider.getMonthsMap().size(), is(2));
        assertNotNull(dataProvider.getMonthsMap().get(monthKey));
        MeasurementStatisticMonth month = dataProvider.getMonthsMap().get(monthKey);
        assertThat(month.getRelatedWeeks().size(), is(0));
        MeasurementStatisticMonth previousMonth = dataProvider.getMonthsMap().get(previousMonthKey);
        assertThat(previousMonth.getRelatedWeeks().size(), is(1));
        assertNotNull(previousMonth.getRelatedWeeks().get(weekKey));

        assertThat(week, is(previousMonth.getRelatedWeeks().get(weekKey)));

        assertThat(week.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(month.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(previousMonth.getSystolic().getValueCount(), is(BigDecimal.ZERO));
    }

    @Test
    public void testWithTwoItemsInSameWeekButDifferentMonth() {
        DateTime dateTime = DateTime.parse("2017-01-31T12:00:00.000+01:00");
        Long weekKey = dateTime.withDayOfWeek(1).withMillisOfDay(0).getMillis();
        Long monthKey = dateTime.withDayOfMonth(1).withMillisOfDay(0).getMillis();
        Long nextMonthKey = new DateTime(monthKey).plusMonths(1).getMillis();

        DataProvider.addToStatistics(new MeasurementData(dateTime, 100, 80, 60, false, null));
        DataProvider.addToStatistics(new MeasurementData(dateTime.withDayOfWeek(5), 100, 80, 60, false, null)); // same week, next Month

        assertThat(dataProvider.getWeeksMap().size(), is(1));
        assertNotNull(dataProvider.getWeeksMap().get(weekKey));
        MeasurementStatisticWeek week = dataProvider.getWeeksMap().get(weekKey);

        assertThat(dataProvider.getMonthsMap().size(), is(2));
        assertNotNull(dataProvider.getMonthsMap().get(monthKey));
        MeasurementStatisticMonth month = dataProvider.getMonthsMap().get(monthKey);
        assertThat(month.getRelatedWeeks().size(), is(0));
        MeasurementStatisticMonth nextMonth = dataProvider.getMonthsMap().get(nextMonthKey);
        assertThat(nextMonth.getRelatedWeeks().size(), is(1));
        assertNotNull(nextMonth.getRelatedWeeks().get(weekKey));

        assertThat(week, is(nextMonth.getRelatedWeeks().get(weekKey)));

        assertThat(week.getSystolic().getValueCount(), is(BigDecimal.valueOf(2)));
        assertThat(month.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(nextMonth.getSystolic().getValueCount(), is(BigDecimal.ONE));
    }

    @Test
    public void testWithTwoItemsInDifferentWeeksAndDifferentMonth() {
        DateTime dateTime = DateTime.parse("2017-01-27T12:00:00.000+01:00");
        Long weekKey = dateTime.withDayOfWeek(1).withMillisOfDay(0).getMillis();
        Long nextWeekKey = new DateTime(weekKey).plusWeeks(1).getMillis();
        Long monthKey = dateTime.withDayOfMonth(1).withMillisOfDay(0).getMillis();
        Long nextMonthKey = new DateTime(monthKey).plusMonths(1).getMillis();

        DataProvider.addToStatistics(new MeasurementData(dateTime, 100, 80, 60, false, null));
        DataProvider.addToStatistics(new MeasurementData(dateTime.plusWeeks(1), 100, 80, 60, false, null)); // next week, next Month

        assertThat(dataProvider.getWeeksMap().size(), is(2));
        assertNotNull(dataProvider.getWeeksMap().get(weekKey));
        MeasurementStatisticWeek week = dataProvider.getWeeksMap().get(weekKey);
        assertNotNull(dataProvider.getWeeksMap().get(nextWeekKey));
        MeasurementStatisticWeek nextWeek = dataProvider.getWeeksMap().get(nextWeekKey);

        assertThat(dataProvider.getMonthsMap().size(), is(2));
        assertNotNull(dataProvider.getMonthsMap().get(monthKey));
        MeasurementStatisticMonth month = dataProvider.getMonthsMap().get(monthKey);
        assertThat(month.getRelatedWeeks().size(), is(1));
        assertNotNull(month.getRelatedWeeks().get(weekKey));
        MeasurementStatisticMonth nextMonth = dataProvider.getMonthsMap().get(nextMonthKey);
        assertThat(nextMonth.getRelatedWeeks().size(), is(1));
        assertNotNull(nextMonth.getRelatedWeeks().get(nextWeekKey));

        assertThat(week, is(month.getRelatedWeeks().get(weekKey)));
        assertThat(nextWeek, is(nextMonth.getRelatedWeeks().get(nextWeekKey)));

        assertThat(week.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(nextWeek.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(month.getSystolic().getValueCount(), is(BigDecimal.ONE));
        assertThat(nextMonth.getSystolic().getValueCount(), is(BigDecimal.ONE));
    }
}
