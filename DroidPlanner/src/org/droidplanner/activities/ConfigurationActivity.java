package org.droidplanner.activities;

import org.droidplanner.R;
import org.droidplanner.activities.helpers.SuperUI;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.fragments.ChecklistFragment;
import org.droidplanner.fragments.ParamsFragment;
import org.droidplanner.fragments.SetupFailsafeFragment;
import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.SetupSensorFragment;
import org.droidplanner.fragments.TuningFragment;
import org.droidplanner.widgets.viewPager.TabPageIndicator;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

public class ConfigurationActivity extends SuperUI {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

		final ConfigurationPagerAdapter pagerAdapter = new ConfigurationPagerAdapter(
				getApplicationContext(), getSupportFragmentManager());

		final ViewPager viewPager = (ViewPager) findViewById(R.id.configuration_pager);
		viewPager.setAdapter(pagerAdapter);

		final TabPageIndicator tabIndicator = (TabPageIndicator) findViewById(R.id.configuration_tab_strip);
		tabIndicator.setViewPager(viewPager);

		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
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
			switch (position) {
			case 0:
				return new TuningFragment();
			case 1:
				return new SetupRadioFragment();
			case 2:
				return new SetupSensorFragment();
			case 3:
				return new SetupFailsafeFragment();
			case 4:
				return new ChecklistFragment();
			case 5:
				return new ParamsFragment();
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 6;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return mContext.getString(R.string.screen_tuning);
			case 1:
				return mContext.getText(R.string.screen_rc);
			case 2:
				return mContext.getString(R.string.screen_cal);
			case 3:
				return mContext.getString(R.string.screen_failsafe);
			case 4:
				return mContext.getString(R.string.screen_checklist);
			case 5:
				return mContext.getText(R.string.screen_parameters);
			default:
				return null;
			}
		}
	}

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] { {}, {} };
	}
}
