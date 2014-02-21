package org.droidplanner.activities;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import org.droidplanner.R;
import org.droidplanner.activities.helpers.SuperUI;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.fragments.ChecklistFragment;
import org.droidplanner.fragments.ParamsFragment;
import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.SetupSensorFragment;
import org.droidplanner.fragments.TuningFragment;
import org.droidplanner.widgets.viewPager.TabPageIndicator;

/**
 * This class implements and handles the various ui used for the drone configuration.
 */
public class ConfigurationActivity extends SuperUI {

    private static final String TAG = ConfigurationActivity.class.getSimpleName();

    /**
     * Holds the list of configuration screens this activity supports.
     */
    private static final Class<? extends Fragment>[] sConfigurationFragments = new Class[]{
            TuningFragment.class,
            SetupRadioFragment.class,
            SetupSensorFragment.class,
            ChecklistFragment.class,
            ParamsFragment.class
    };

    /**
     * Holds the title resources for the configuration screens.
     */
    private static final int[] sConfigurationFragmentTitlesRes = {
            R.string.screen_tuning,
            R.string.screen_rc,
            R.string.screen_cal,
            R.string.screen_checklist,
            R.string.screen_parameters
    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

        //Check that the arrays are the same length.
        if(sConfigurationFragments.length != sConfigurationFragmentTitlesRes.length){
            throw new IllegalStateException("The fragment and title resource arrays must match in" +
                    " length.");
        }

        final Context context = getApplicationContext();

        final ConfigurationPagerAdapter pagerAdapter = new ConfigurationPagerAdapter(
                context, getSupportFragmentManager());

        final ViewPager viewPager = (ViewPager) findViewById(R.id.configuration_pager);
        viewPager.setAdapter(pagerAdapter);

        /*
        Figure out if we're running on a tablet like device, or a phone.
        The phone layout doesn't the tab strip, so the tabIndicator will be null.
         */
        final TabPageIndicator tabIndicator = (TabPageIndicator) findViewById(R.id
                .configuration_tab_strip);
        final boolean isPhone = tabIndicator == null;

        if (!isPhone) {
            tabIndicator.setViewPager(viewPager);
        }

		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);

            if(isPhone){
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

                //Display the sections as an action bar drop down list.
                actionBar.setListNavigationCallbacks(new ConfigurationSpinnerAdapter(context,
                        R.layout.spinner_configuration_screen_item), new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                        viewPager.setCurrentItem(itemPosition, true);
                        return true;
                    }
                });

                viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

                    @Override
                    public void onPageSelected(int i) {
                        actionBar.setSelectedNavigationItem(i);
                    }
                });
            }
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		super.onDroneEvent(event, drone);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public CharSequence[][] getHelpItems() {
        return new CharSequence[][] { {}, {} };
    }

	/**
	 * This is the fragment pager adapter to handle the tabs of the
	 * Configuration activity.
	 *
	 * @since 1.2.0
	 */
	private static class ConfigurationPagerAdapter extends FragmentPagerAdapter {

		/**
		 * Application context object used to retrieve the tabs' title.
		 *
		 * @since 1.2.0
		 */
		private final Context mContext;

		public ConfigurationPagerAdapter(Context context, FragmentManager fm) {
			super(fm);
			mContext = context;
		}

		@Override
		public Fragment getItem(int position) {
            try {
                return sConfigurationFragments[position].newInstance();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return null;
		}

		@Override
		public int getCount() {
			return sConfigurationFragments.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
            final int titleRes = sConfigurationFragmentTitlesRes[position];
            return mContext.getText(titleRes);
		}
	}

    private static class ConfigurationSpinnerAdapter extends ArrayAdapter<CharSequence> {

        public ConfigurationSpinnerAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount(){
            return sConfigurationFragmentTitlesRes.length;
        }

        @Override
        public CharSequence getItem(int position){
            return getContext().getText(sConfigurationFragmentTitlesRes[position]);
        }

    }

}
