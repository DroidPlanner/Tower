package com.droidplanner.activitys;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.activitys.helpers.InfoMenu;
import com.droidplanner.activitys.helpers.ScreenOrientation;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.widgets.spinners.SelectModeSpinner.OnModeSpinnerSelectedListener;

public abstract class NewSuperUI extends SuperActivity implements ConnectionStateListner, OnModeSpinnerSelectedListener {
	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoMenu infoMenu;
	
	public NewSuperUI() {
		super();        
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenOrientation.unlock();
		app.conectionListner = this;

		drone.MavClient.queryConnectionState();
		infoMenu = new InfoMenu(drone);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		infoMenu.inflateMenu(menu, getMenuInflater());	
		infoMenu.setupModeSpinner(this, this);
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

	@Override
	public void OnModeSpinnerSelected(ApmModes apmModes) {
		// TODO Auto-generated method stub
		
	}

}