package com.droidplanner.activitys.helpers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.DroidPlannerApp.ConnectionStateListner;

public abstract class SuperUI extends SuperActivity implements ConnectionStateListner {
	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoMenu infoMenu;
	
	public SuperUI() {
		super();        
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		screenOrientation.unlock();
		infoMenu = new InfoMenu(drone);
	}

	@Override
	protected void onStart() {
		super.onStart();
		app.conectionListner = this;
		drone.MavClient.queryConnectionState();
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
		invalidateOptionsMenu();		
		/*
		if(armButton != null){
			armButton.setEnabled(false);
		}*/
		screenOrientation.unlock();
	}

	public void notifyConnected() {
		invalidateOptionsMenu();
		
		/*
		if(armButton != null){
			armButton.setEnabled(true);
		}
		*/
		screenOrientation.requestLock();
	}

}