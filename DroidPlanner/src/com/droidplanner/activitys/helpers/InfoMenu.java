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
	private MenuItem signalRSSI;
	private MenuItem signalRemRSSI;
	private MenuItem signalNoise;
	private MenuItem signalRemNoise;
	private MenuItem signalRemFade;
	private MenuItem signalFade;
	private Context context;
	private ProgressDialog pd;
	private int pdTitle;
	
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
			signalRSSI = menu.findItem(R.id.bar_signal_rssi);
			signalRemRSSI =menu.findItem(R.id.bar_signal_rssirem);
			signalNoise =menu.findItem(R.id.bar_signal_noise);
			signalRemNoise = menu.findItem(R.id.bar_signal_noiserem);
			signalFade = menu.findItem(R.id.bar_signal_fade);
			signalRemFade = menu.findItem(R.id.bar_signal_faderem);
			mode = (SelectModeSpinner) menu.findItem(R.id.bar_mode)
					.getActionView();

			timer = new TimerView(propeler);
			drone.setHomeChangedListner(this);
			drone.addInfoListener(this);
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
		signal.setTitle(String.format("%d%%",drone.radio.getSignalStrength()));
		signalRSSI.setTitle(String.format("RSSI %d dB",drone.radio.getRssi()));
		signalRemRSSI.setTitle(String.format("RemRSSI %d dB",drone.radio.getRemRssi()));
		signalNoise.setTitle(String.format("Noise %d dB",drone.radio.getNoise()));
		signalRemNoise.setTitle(String.format("RemNoise %d dB",drone.radio.getRemNoise()));
		signalFade.setTitle(String.format("Fade %d dB",drone.radio.getFadeMargin()));
		signalRemFade.setTitle(String.format("RemFade %d dB",drone.radio.getRemFadeMargin()));
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
		} else {
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
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
		pd = new ProgressDialog(context);
		switch (wpEvent) {
		case WP_UPLOAD:
			pdTitle = R.string.wpevent_upload;
			break;
		case WP_DOWNLOAD:
			pdTitle = R.string.wpevent_download;
			break;
		default:
			break;
		}
		pd.setTitle(pdTitle);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(true);
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(true);

		pd.show();
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (pd != null) {
			if (wpEvent != WaypointEvent_Type.WP_RETRY) {
				
				if(wpEvent.equals(WaypointEvent_Type.WP_CONTINUE))
					pd.setTitle(pdTitle);
				
				if (pd.isIndeterminate()) {
					pd.setIndeterminate(false);
					pd.setMax(count);
				}
				pd.setProgress(index);
			} 
			else {
				pd.setIndeterminate(true);
				pd.setTitle(R.string.wpevent_retry);
			}
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
