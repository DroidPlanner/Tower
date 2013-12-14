package com.droidplanner.activitys.helpers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.MAVLink.MavLinkHeartbeat;

public abstract class SuperUI extends SuperActivity implements ConnectionStateListner {
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
		app.conectionListner = this;
		drone.MavClient.queryConnectionState();
		infoMenu = new InfoMenu(drone,this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		infoMenu = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		infoMenu.inflateMenu(menu, getMenuInflater());	
		infoMenu.setupModeSpinner(this);
		return super.onCreateOptionsMenu(menu);
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