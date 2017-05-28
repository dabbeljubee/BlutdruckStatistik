package de.dabbeljubee.blutdruckstatistik;

import android.view.View;
import android.widget.TextView;

public enum EnumFocusView {
    SYSTOLIC(R.id.systolicValue),
    DIASTOLIC(R.id.diastolicValue),
    PULSE(R.id.pulseValue);

    int id;

    EnumFocusView(int id){
        this.id = id;
    }

    TextView getFocusView(View view) {
        return (TextView) view.getRootView().findViewById(id);
    }
}
