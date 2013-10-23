package com.droidplanner.activitys.helpers;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.InfoListner;

public class InfoMenu implements InfoListner {
	private Drone drone;
	private MenuItem battery;
	private MenuItem gps;

	public InfoMenu(Drone drone) {
		this.drone = drone;
	}

	public void inflateMenu(Menu menu, MenuInflater menuInflater) {
		if (drone.MavClient.isConnected()) {
			drone.setInfoListner(this);
			menuInflater.inflate(R.menu.menu_newui_connected, menu);	
			battery = menu.findItem(R.id.bar_battery);
			gps = menu.findItem(R.id.bar_gps);
			
		}else{
			menuInflater.inflate(R.menu.menu_newui_disconnected, menu);
		}
	}

	@Override
	public void onInfoUpdate() {
		battery.setTitle(String.format("%2.1fv, %2.0f%%",drone.battery.getBattVolt(),drone.battery.getBattRemain()));
		gps.setTitle(String.format("%d, %s",drone.GPS.getSatCount(),drone.GPS.getFixType()));
	}




}
