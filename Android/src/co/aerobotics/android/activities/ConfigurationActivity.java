package co.aerobotics.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.fragments.ChecklistFragment;
import co.aerobotics.android.fragments.calibration.compass.FragmentSetupCompass;
import co.aerobotics.android.fragments.calibration.imu.FragmentSetupIMU;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

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

    private int mConfigScreenId = R.id.navigation_checklist;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(co.aerobotics.android.R.layout.activity_configuration);
        MixpanelAPI mixpanelAPI = MixpanelAPI.getInstance(this, DroidPlannerApp.getInstance().getMixpanelToken());
        mixpanelAPI.track("FPA: OnCreateCheckListActivity");

        if(savedInstanceState != null){
            mConfigScreenId = savedInstanceState.getInt(EXTRA_CONFIG_SCREEN_ID, mConfigScreenId);
        }

		handleIntent(getIntent());
	}

    @Override
    protected int getToolbarId() {
        return co.aerobotics.android.R.id.actionbar_toolbar;
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
                    .replace(co.aerobotics.android.R.id.configuration_screen, getFragmentForId(configScreenId))
                    .commit();
        }
	}

    public Fragment getCurrentFragment(){
        return getSupportFragmentManager().findFragmentById(co.aerobotics.android.R.id.configuration_screen);
    }

    private Fragment getFragmentForId(int fragmentId){
        final Fragment fragment;
        switch(fragmentId){

            case co.aerobotics.android.R.id.navigation_compass_calibration:
                fragment = new FragmentSetupCompass();
                break;

            case co.aerobotics.android.R.id.navigation_checklist:
            default:
                fragment = new ChecklistFragment();
                break;
        }

        return fragment;
    }

    private int getIdForFragment(Fragment fragment){
        if(fragment instanceof FragmentSetupIMU){
            return 0;
        }
        else if(fragment instanceof FragmentSetupCompass){
            return co.aerobotics.android.R.id.navigation_compass_calibration;
        }
        else if(fragment instanceof ChecklistFragment){
            return co.aerobotics.android.R.id.navigation_checklist;
        }
        else {
            return 1;
        }
    }
}
