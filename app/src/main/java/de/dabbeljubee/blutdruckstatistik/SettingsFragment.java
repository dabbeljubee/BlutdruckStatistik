package de.dabbeljubee.blutdruckstatistik;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import de.dabbeljubee.blutdruckstatistik.Logic.DataProvider;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class SettingsFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final Logger LOGGER = Logger.getLogger("PreferenceFragment");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initializeSummary(getPreferenceScreen().getPreference(i));
        }
    }

    private void initializeSummary(Preference p)
    {
        LOGGER.fine(String.format(Locale.GERMAN, "Lookup %s of type %s", p.getKey(), p.getClass().toString()));
        if(p instanceof PreferenceCategory) {
            PreferenceCategory editTextPref = (PreferenceCategory)p;
            LOGGER.fine(String.format(Locale.GERMAN, "%s has %d items", p.getKey(), ((PreferenceCategory) p).getPreferenceCount()));
            for (int i = 0; i < editTextPref.getPreferenceCount(); i++) {
                initializeSummary(editTextPref.getPreference(i));
            }
        }
        if(p instanceof EditTextPreference) {
            p.setSummary(DataProvider.getPreferences().getString(p.getKey(), "0"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // we want to watch the preference values' changes
        DataProvider.getPreferences().registerOnSharedPreferenceChangeListener(this);

        Map<String, ?> preferencesMap = DataProvider.getPreferences().getAll();
        // iterate through the preference entries and update their summary if they are an instance of EditTextPreference
        for (Map.Entry<String, ?> preferenceEntry : preferencesMap.entrySet()) {
            if (preferenceEntry instanceof EditTextPreference) {
                LOGGER.fine(((EditTextPreference) preferenceEntry).getTitle().toString());
                updateSummary((EditTextPreference) preferenceEntry);
            }
        }

        DataProvider.getDataProvider().updatePreferences(getActivity().getBaseContext());
    }

    @Override
    public void onPause() {
        DataProvider.getPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preferenceToUpdate = findPreference(key);
        if (preferenceToUpdate instanceof EditTextPreference) {
            updateSummary((EditTextPreference) preferenceToUpdate);
        }

        LOGGER.fine(String.format("Reload preferences after update of %s", key));
        DataProvider.getDataProvider().updatePreferences(getActivity().getBaseContext());
    }

    private void updateSummary(EditTextPreference preference) {
        // set the EditTextPreference's summary value to its current text
        preference.setSummary(preference.getText());
    }
}
