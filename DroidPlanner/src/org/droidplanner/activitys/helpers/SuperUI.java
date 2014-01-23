package org.droidplanner.activitys.helpers;

import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.gcs.GCSHeartbeat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public abstract class SuperUI extends SuperActivity implements OnDroneListner {
	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoMenu infoMenu;
	private GCSHeartbeat gcsHeartbeat;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		screenOrientation.unlock();
		gcsHeartbeat = new GCSHeartbeat(drone,1);
	}

	@Override
	protected void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		drone.MavClient.queryConnectionState();
		drone.events.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
		infoMenu = null;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (infoMenu != null) {
			infoMenu.onDroneEvent(event, drone);
		}
		switch (event) {
		case CONNECTED:
			gcsHeartbeat.setActive(true);
			invalidateOptionsMenu();
			screenOrientation.requestLock();
			break;
		case DISCONNECTED:
			gcsHeartbeat.setActive(false);
			invalidateOptionsMenu();
			screenOrientation.unlock();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		infoMenu = new InfoMenu(drone,this);
		infoMenu.inflateMenu(menu, getMenuInflater());	
		infoMenu.setupModeSpinner(this);
		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		infoMenu.forceViewsUpdate();
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(infoMenu.onOptionsItemSelected(item))
            return true;
		return super.onOptionsItemSelected(item);
	}
}