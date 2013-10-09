package com.droidplanner.activitys;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperFlightActivity;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.HudFragment;
import com.droidplanner.fragments.RCFragment;
import com.droidplanner.widgets.viewPager.MapViewPager;

public class RCActivity extends SuperFlightActivity implements
		DroneTypeListner {

	AdapterHudMap mAdapter;
	MapViewPager mPager;

	@Override
	public int getNavigationItem() {
		return 2;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_rc);

		mAdapter = new AdapterHudMap(getFragmentManager());
		mPager = (MapViewPager) findViewById(R.id.rcPager);
		if (mPager != null) {
			mPager.setAdapter(mAdapter);
			mPager.setSwipeMarginWidth(40);
		}

		drone.mission.addOnWaypointsChangedListner(this);
		drone.setDroneTypeChangedListner(this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_rc, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_zoom:
			if (mapFragment != null)
				mapFragment.zoomToLastKnowPosition();
			return true;
		case R.id.menu_clearFlightPath:
			if (mapFragment != null)
				mapFragment.clearFlightPath();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public class AdapterHudMap extends FragmentPagerAdapter {
		static final int NUM_ADAPTER_FRAGMENT_ITEMS = 2;

		public AdapterHudMap(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUM_ADAPTER_FRAGMENT_ITEMS;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				hudFragment = new HudFragment();
				return hudFragment;
			case 1:
				mapFragment = new FlightMapFragment();
				return mapFragment;
			default:
				return null;
			}
		}
	}
	

	//Handle movement of wikipad joystick
	@Override
	public boolean onGenericMotionEvent(MotionEvent ev){
		RCFragment rcFragment = (RCFragment) getFragmentManager().findFragmentById(R.id.rcFragment);
		return rcFragment.physicalJoyMoved(ev);
	}
}