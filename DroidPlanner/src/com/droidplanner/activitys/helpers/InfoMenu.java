package com.droidplanner.activitys.helpers;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HomeDistanceChangedListner;
import com.droidplanner.drone.DroneInterfaces.InfoListner;
import com.droidplanner.widgets.spinners.SelectModeSpinner;

public class InfoMenu implements InfoListner, HomeDistanceChangedListner {
	private Drone drone;
	private MenuItem battery;
	private MenuItem gps;
	private MenuItem propeler;
	private MenuItem home;
	private MenuItem signal;
	public SelectModeSpinner mode;

	public InfoMenu(Drone drone) {
		this.drone = drone;
	}

	public void inflateMenu(Menu menu, MenuInflater menuInflater) {
		if (drone.MavClient.isConnected()) {
			menuInflater.inflate(R.menu.menu_newui_connected, menu);
			battery = menu.findItem(R.id.bar_battery);
			gps = menu.findItem(R.id.bar_gps);
			propeler = menu.findItem(R.id.bar_propeller);
			home = menu.findItem(R.id.bar_home);
			signal = menu.findItem(R.id.bar_signal);
			mode = (SelectModeSpinner) menu.findItem(R.id.bar_mode).getActionView();

			drone.setHomeChangedListner(this);
			drone.setInfoListner(this);

		} else {
			menuInflater.inflate(R.menu.menu_newui_disconnected, menu);
		}
	}

	public void onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.bar_home:
			drone.waypointMananger.getWaypoints();
			break;
		}
	}

	@Override
	public void onInfoUpdate() {
		battery.setTitle(String.format("%2.1fv, %2.0f%%",
				drone.battery.getBattVolt(), drone.battery.getBattRemain()));
		gps.setTitle(String.format("%d, %s", drone.GPS.getSatCount(),
				drone.GPS.getFixType()));
	}

	@Override
	public void onDistanceToHomeHasChanged() {
		home.setTitle(drone.home.getDroneDistanceToHome().toString());
	}

	public void setupModeSpinner(Context context) {
		if (mode!=null) {
			mode.buildSpinner(context, drone);
		}
		
	}
}
