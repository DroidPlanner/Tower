package com.droidplanner.activitys.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HomeDistanceChangedListner;
import com.droidplanner.drone.DroneInterfaces.InfoListner;
import com.droidplanner.drone.DroneInterfaces.OnStateListner;
import com.droidplanner.drone.DroneInterfaces.OnWaypointManagerListener;
import com.droidplanner.drone.variables.mission.WaypointEvent_Type;
import com.droidplanner.widgets.TimerView;
import com.droidplanner.widgets.spinners.SelectModeSpinner;

public class InfoMenu implements InfoListner, HomeDistanceChangedListner,
		OnStateListner, OnWaypointManagerListener {
	private Drone drone;
	private MenuItem battery;
	private MenuItem gps;
	private MenuItem propeler;
	private MenuItem home;
	private MenuItem signal;
	private Context context;
	private ProgressDialog pd;

	public SelectModeSpinner mode;
	
	private TimerView timer;

	public InfoMenu(Drone drone, Context context) {
		this.drone = drone;
		this.context = context;
		this.drone.waypointMananger.setWaypointManagerListener(this);
	}

	public void inflateMenu(Menu menu, MenuInflater menuInflater) {
		if (drone.MavClient.isConnected()) {
			menuInflater.inflate(R.menu.menu_newui_connected, menu);
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

		} else {
			menuInflater.inflate(R.menu.menu_newui_disconnected, menu);
		}
	}

	public void onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.bar_home:
			drone.waypointMananger.getWaypoints();
			break;
		case R.id.menu_send_mission:
			drone.mission.sendMissionToAPM();
			break;
		case R.id.menu_load_mission:
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

	@Override
	public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {
		if(pd!=null){
			pd.dismiss();
			pd = null;
		}
		
		pd = new ProgressDialog(context);
		switch(wpEvent){
		case WP_UPLOAD:
			pd.setTitle(R.string.wpevent_upload);
			break;
		case WP_DOWNLOAD:
			pd.setTitle(R.string.wpevent_download);
			break;
		}
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(true);
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(true);

		pd.show();
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (pd != null) {
			if (pd.isIndeterminate()) {
				pd.setIndeterminate(false);
				pd.setMax(count);
			}
			pd.setProgress(index);
		}
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}		
	}	


}
