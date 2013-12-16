package com.droidplanner.activitys.helpers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.MAVLink.MavLinkHeartbeat;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;

public abstract class SuperUI extends SuperActivity implements ConnectionStateListner, OnDroneListner {
	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoMenu infoMenu;
	private MavLinkHeartbeat mavLinkHeartbeat;
	
	public SuperUI() {
		super();        
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		screenOrientation.unlock();
		mavLinkHeartbeat = new MavLinkHeartbeat(drone,1);
	}

	@Override
	protected void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		app.conectionListner = this;
		drone.MavClient.queryConnectionState();
	}

	@Override
	protected void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
		infoMenu = null;
	}
	
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {	
		if (infoMenu!=null) {
			infoMenu.onDroneEvent(event,drone);			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		infoMenu = new InfoMenu(drone,this);
		infoMenu.inflateMenu(menu, getMenuInflater());	
		infoMenu.setupModeSpinner(this);		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		infoMenu.forceViewsUpdate();
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		infoMenu.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	public void notifyDisconnected() {
		mavLinkHeartbeat.setActive(false);
		invalidateOptionsMenu();		
		/*
		if(armButton != null){
			armButton.setEnabled(false);
		}*/
		screenOrientation.unlock();
	}

	public void notifyConnected() {
		mavLinkHeartbeat.setActive(true);
		invalidateOptionsMenu();
		
		/*
		if(armButton != null){
			armButton.setEnabled(true);
		}
		*/
		screenOrientation.requestLock();
	}
}