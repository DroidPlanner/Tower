package org.droidplanner.android.fragments.helpers;

import org.droidplanner.R;
import org.droidplanner.android.fragments.TelemetryFragment;
import org.droidplanner.android.fragments.mode.FlightModePanel;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Fredia Huya-Kouadio
 */
public class FlightSlidingDrawerContent extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(
				R.layout.fragment_flight_sliding_drawer_content, container,
				false);

		final ViewPager viewPager = (ViewPager) view
				.findViewById(R.id.sliding_drawer_content);
		viewPager.setAdapter(new FlightSlidingDrawerAdapter(
				getChildFragmentManager(), getActivity()
						.getApplicationContext()));

		return view;
	}

	private static class FlightSlidingDrawerAdapter extends
			FragmentPagerAdapter {

		private final Context mContext;

		public FlightSlidingDrawerAdapter(FragmentManager fm, Context context) {
			super(fm);
			mContext = context;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return new TelemetryFragment();

			case 1:
				return new FlightModePanel();

			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return mContext.getString(R.string.telemetry_label);

			case 1:
				return mContext.getString(R.string.flight_modes_label);

			default:
				return null;
			}
		}
	}
}