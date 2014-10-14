package org.droidplanner.android.activities;

import org.droidplanner.R;
import org.droidplanner.android.fragments.ChecklistFragment;
import org.droidplanner.android.fragments.ParamsFragment;
import org.droidplanner.android.fragments.SetupRadioFragment;
import org.droidplanner.android.fragments.SetupSensorFragment;
import org.droidplanner.android.fragments.TuningFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * This class implements and handles the various ui used for the drone
 * configuration.
 */
public class ConfigurationActivity extends DrawerNavigationUI {

	/**
	 * Used as logging tag.
	 */
	private static final String TAG = ConfigurationActivity.class.getSimpleName();

	public static final String EXTRA_CONFIG_SCREEN_ID = ConfigurationActivity.class.getPackage()
			.getName() + ".EXTRA_CONFIG_SCREEN_ID";

    private int mConfigScreenId = R.id.navigation_params;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

        if(savedInstanceState != null){
            mConfigScreenId = savedInstanceState.getInt(EXTRA_CONFIG_SCREEN_ID, mConfigScreenId);
        }

		handleIntent(getIntent());
	}

    @Override
    protected int getNavigationDrawerEntryId() {
        return mConfigScreenId;
    }

    @Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
	}

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_CONFIG_SCREEN_ID, mConfigScreenId);
    }

	private void handleIntent(Intent intent) {
		final int configScreenId = intent.getIntExtra(EXTRA_CONFIG_SCREEN_ID, mConfigScreenId);
        final Fragment currentFragment = getCurrentFragment();
        if(currentFragment == null || getIdForFragment(currentFragment) != configScreenId){
            mConfigScreenId = configScreenId;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.configuration_screen, getFragmentForId(configScreenId))
                    .commit();
        }
	}

    private Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.configuration_screen);
    }

    private Fragment getFragmentForId(int fragmentId){
        final Fragment fragment;
        switch(fragmentId){
            case R.id.navigation_tuning:
                fragment = new TuningFragment();
                break;

            case R.id.navigation_radio:
                fragment = new SetupRadioFragment();
                break;

            case R.id.navigation_calibration:
                fragment = new SetupSensorFragment();
                break;

            case R.id.navigation_checklist:
                fragment = new ChecklistFragment();
                break;

            case R.id.navigation_params:
            default:
                fragment = new ParamsFragment();
                break;
        }

        return fragment;
    }

    private int getIdForFragment(Fragment fragment){
        if(fragment instanceof TuningFragment){
            return R.id.navigation_tuning;
        }
        else if(fragment instanceof SetupRadioFragment){
            return R.id.navigation_radio;
        }
        else if(fragment instanceof SetupSensorFragment){
            return R.id.navigation_calibration;
        }
        else if(fragment instanceof ChecklistFragment){
            return R.id.navigation_checklist;
        }
        else {
            return R.id.navigation_params;
        }
    }

    @Override
    public CharSequence[][] getHelpItems() {
        return new CharSequence[][] { {}, {} };
    }
}
