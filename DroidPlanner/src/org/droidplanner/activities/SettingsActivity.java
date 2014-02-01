package org.droidplanner.activities;

import org.droidplanner.activities.helpers.SuperUI;
import org.droidplanner.fragments.SettingsFragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import org.droidplanner.R;
import org.droidplanner.glass.utils.GlassUtils;

/**
 * This activity holds the SettingsFragment.
 */
public class SettingsActivity extends SuperUI {

    public void onCreate(Bundle savedInstanceState) {
        //Update the theme if we're on a glass device
        if(GlassUtils.isGlassDevice()){
            setTheme(android.R.style.Theme_DeviceDefault);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FragmentManager fm = getFragmentManager();
        Fragment settingsFragment = fm.findFragmentById(R.id.fragment_settings_layout);
        if(settingsFragment == null){
            settingsFragment = new SettingsFragment();
            fm.beginTransaction().add(R.id.fragment_settings_layout, settingsFragment).commit();
        }
    }

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] { {}, {} };
	}
}
