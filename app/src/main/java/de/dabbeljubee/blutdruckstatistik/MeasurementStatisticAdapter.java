package de.dabbeljubee.blutdruckstatistik;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementStatisticItem;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementStatisticMonth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public abstract class MeasurementStatisticAdapter extends BaseExpandableListAdapter {

    private static final Logger LOGGER = Logger.getLogger("MeasurementStatisticAdapter");
    private boolean updateHeader = true;

    private List<MeasurementStatisticMonth> months = DataProvider.getDataProvider().getMonthsDescending();

    void update(boolean updateHeader) {
        months = DataProvider.getDataProvider().getMonthsDescending();
        this.updateHeader = updateHeader;
    }

    @Override
    public int getGroupCount() {
        return months.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return months.get(groupPosition).getRelatedWeeks().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return months.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return new ArrayList<>(months.get(groupPosition).getRelatedWeeks().descendingMap().values()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final TableRow groupRow = getStatisticsRowView(convertView, parent, months.get(groupPosition));

        if (updateHeader) {
            formatHeader(parent.getRootView());
            updateHeader = false;
        }

        LOGGER.fine(String.format(Locale.GERMAN, "ChildNo: %d", ((TableRow)groupRow.getChildAt(0)).getChildCount()));
        for (int i = 0; i < ((TableRow)groupRow.getChildAt(0)).getChildCount(); i++) {
            View view = ((TableRow)groupRow.getChildAt(0)).getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView)view;
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        return groupRow;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        return getStatisticsRowView(convertView, parent, (MeasurementStatisticItem) getChild(groupPosition, childPosition));
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private TableRow getStatisticsRowView(View convertView, ViewGroup parent, MeasurementStatisticItem item) {
        TableRow row;
        if (null == convertView) {
            row = new TableRow(parent.getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater layoutInflater;
            layoutInflater = (LayoutInflater) parent.getContext().getSystemService(inflater);
            layoutInflater.inflate(R.layout.statistic_row, row, true);
        } else {
            row = (TableRow) convertView;
        }

        TextView dateTime = (TextView) row.findViewById(R.id.statistic_time);
        dateTime.setText(item.getFirstDayAsString(row.getResources()));

        fillRowWithData(row, item);

        return row;
    }

    protected abstract void fillRowWithData(TableRow row, MeasurementStatisticItem item);
    protected abstract void formatHeader(View rootView);
}
