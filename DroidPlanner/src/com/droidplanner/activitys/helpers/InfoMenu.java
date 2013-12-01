package com.droidplanner.activitys.helpers;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HomeDistanceChangedListner;
import com.droidplanner.drone.DroneInterfaces.InfoListner;
import com.droidplanner.drone.DroneInterfaces.OnStateListner;
import com.droidplanner.widgets.TimerView;
import com.droidplanner.widgets.spinners.SelectModeSpinner;

public class InfoMenu implements InfoListner, HomeDistanceChangedListner,
		OnStateListner {
	private Drone drone;
	private MenuItem battery;
	private MenuItem gps;
	private MenuItem propeler;
	private MenuItem home;
	private MenuItem signal;
	public SelectModeSpinner mode;

	private TimerView timer;

	public InfoMenu(Drone drone) {
		this.drone = drone;
	}

    public void inflateMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_newui, menu);
        battery = menu.findItem(R.id.bar_battery);
        gps = menu.findItem(R.id.bar_gps);
        propeler = menu.findItem(R.id.bar_propeller);
        home = menu.findItem(R.id.bar_home);
        signal = menu.findItem(R.id.bar_signal);
        mode = (SelectModeSpinner) menu.findItem(R.id.bar_mode)
                .getActionView();

        timer = new TimerView(propeler);
        drone.setHomeChangedListner(this);
        drone.setInfoListner(this);
        drone.state.addFlightStateListner(this);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        final boolean isConnected = drone.MavClient.isConnected();
        menu.setGroupVisible(R.id.menu_group_connected_state, isConnected);
        menu.setGroupVisible(R.id.menu_group_disconnected_state, !isConnected);
    }

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.bar_home:
			drone.waypointMananger.getWaypoints();
            return true;
		}
        return false;
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
		if (mode != null) {
			mode.buildSpinner(context, drone);
		}
	}

	@Override
	public void onFlightStateChanged() {
		if (drone.state.isFlying()) {
			timer.reStart();
		}else{
			timer.stop();
		}
	}

	@Override
	public void onArmChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailsafeChanged() {
		// TODO Auto-generated method stub

	}


}
