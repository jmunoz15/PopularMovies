package com.jmunoz.popularmovies;

import android.preference.PreferenceFragment;
import android.os.Bundle;

public class SettingsActivityFragment extends PreferenceFragment{

    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
