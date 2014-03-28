package org.droidplanner.android.activities;

import org.droidplanner.R;
import org.droidplanner.android.fragments.SettingsFragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * This activity holds the SettingsFragment.
 */
public class SettingsActivity extends DrawerNavigationUI {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		FragmentManager fm = getFragmentManager();
		Fragment settingsFragment = fm
				.findFragmentById(R.id.fragment_settings_layout);
		if (settingsFragment == null) {
			settingsFragment = new SettingsFragment();
			fm.beginTransaction()
					.add(R.id.fragment_settings_layout, settingsFragment)
					.commit();
		}
	}

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] { {}, {} };
	}
}
