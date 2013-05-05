package com.droidplanner;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.droidplanner.R;

public class SettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	// TODO use more up-to-date code
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
