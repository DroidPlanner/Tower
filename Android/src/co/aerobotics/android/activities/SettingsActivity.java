package co.aerobotics.android.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.MenuItem;

import co.aerobotics.android.activities.helpers.SuperUI;
import co.aerobotics.android.fragments.SettingsFragment;
import co.aerobotics.android.R;

/**
 * This activity holds the SettingsFragment.
 */
public class SettingsActivity extends SuperUI {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		FragmentManager fm = getFragmentManager();
		Fragment settingsFragment = fm.findFragmentById(R.id.fragment_settings_layout);
		if (settingsFragment == null) {
			settingsFragment = new SettingsFragment();
			fm.beginTransaction().add(R.id.fragment_settings_layout, settingsFragment).commit();
		}
	}

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case android.R.id.home:
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
