package org.droidplanner.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.ChecklistFragment;
import org.droidplanner.android.fragments.ParamsFragment;
import org.droidplanner.android.fragments.calibration.compass.FragmentSetupCompass;
import org.droidplanner.android.fragments.calibration.imu.FragmentSetupIMU;

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
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
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

    public Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.configuration_screen);
    }

    private Fragment getFragmentForId(int fragmentId){
        final Fragment fragment;
        switch(fragmentId){
            case R.id.navigation_imu_calibration:
                fragment = new FragmentSetupIMU();
                break;

            case R.id.navigation_compass_calibration:
                fragment = new FragmentSetupCompass();
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
        if(fragment instanceof FragmentSetupIMU){
            return R.id.navigation_imu_calibration;
        }
        else if(fragment instanceof FragmentSetupCompass){
            return R.id.navigation_compass_calibration;
        }
        else if(fragment instanceof ChecklistFragment){
            return R.id.navigation_checklist;
        }
        else {
            return R.id.navigation_params;
        }
    }
}
