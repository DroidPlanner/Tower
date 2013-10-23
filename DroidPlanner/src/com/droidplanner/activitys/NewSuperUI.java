package com.droidplanner.activitys;

import android.os.Bundle;
import android.view.Menu;

import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.ScreenOrientation;
import com.droidplanner.activitys.helpers.SuperActivity;

public abstract class NewSuperUI extends SuperActivity implements ConnectionStateListner {
	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	
	public NewSuperUI() {
		super();        
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		screenOrientation.unlock();
		app.conectionListner = this;

		drone.MavClient.queryConnectionState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (drone.MavClient.isConnected()) {
			getMenuInflater().inflate(R.menu.menu_newui_connected, menu);
		}else{
			getMenuInflater().inflate(R.menu.menu_newui_disconnected, menu);
		}		
		return super.onCreateOptionsMenu(menu);
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