package de.dabbeljubee.blutdruckstatistik;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import de.dabbeljubee.blutdruckstatistik.Logic.MeasurementData;

public class ListViewClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MeasurementData measurementData = (MeasurementData) parent.getItemAtPosition(position);
        Intent intent = new Intent(view.getContext(), EditDataActivity.class);
        intent.putExtra("datetime", measurementData.getDateTime().getMillis());
        view.getContext().startActivity(intent);
    }
}
