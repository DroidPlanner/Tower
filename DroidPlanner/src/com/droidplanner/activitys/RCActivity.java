package com.droidplanner.activitys;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.DroidPlannerApp.OnWaypointReceivedListner;
import com.droidplanner.MAVLink.Drone.DroneTypeListner;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.HudFragment;

public class RCActivity extends SuperFlightActivity implements OnWaypointReceivedListner, DroneTypeListner {
	
	static final int NUM_ADAPTER_FRAGMENT_ITEMS = 2;
	private static HudFragment hudFragment;
	private static FlightMapFragment mapFragment;
	
	MyAdapter mAdapter;
	ViewPager mPager;

	@Override
	int getNavigationItem() {
		return 2;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rc);
		
		hudFragment = new HudFragment();
		mapFragment = new FlightMapFragment();
		
		mAdapter = new MyAdapter(getFragmentManager());
		mPager = (ViewPager)findViewById(R.id.rcPager);
		if (mPager != null) {
			mPager.setAdapter(mAdapter);
		}
		
		app.setWaypointReceivedListner(this);
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

	@SuppressWarnings("unused")
	private void printInputDevicesToLog() {
		int[] inputIds = InputDevice.getDeviceIds();
		Log.d("DEV", "Found " + inputIds.length);
		for (int i = 0; i < inputIds.length; i++) {
			InputDevice inputDevice = InputDevice.getDevice(inputIds[i]);
			Log.d("DEV","name:"+inputDevice.getName()+" Sources:"+inputDevice.getSources());	
		}
	}
	
	public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
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
        			return hudFragment;
        		case 1:
        			return mapFragment;
        		default:
        			return null;
        	}
        }
    }
	
	@Override
	public void onWaypointsReceived() {
		super.onWaypointsReceived();
		mapFragment.updateMissionPath(drone);
		mapFragment.updateHomeToMap(drone);		
	}

	@Override
	public void onDroneTypeChanged() {
		super.onDroneTypeChanged();
		Log.d("DRONE", "Drone type changed");
		mapFragment.droneMarker.updateDroneMarkers();
	}

}