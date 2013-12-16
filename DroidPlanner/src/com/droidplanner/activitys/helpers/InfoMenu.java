package com.droidplanner.activitys.helpers;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.widgets.TimerView;
import com.droidplanner.widgets.spinners.SelectModeSpinner;

public class InfoMenu implements OnDroneListner {
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

	public SelectModeSpinner mode;

	private TimerView timer;

	public InfoMenu(Drone drone, Context context) {
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
			signalRSSI = menu.findItem(R.id.bar_signal_rssi);
			signalRemRSSI = menu.findItem(R.id.bar_signal_rssirem);
			signalNoise = menu.findItem(R.id.bar_signal_noise);
			signalRemNoise = menu.findItem(R.id.bar_signal_noiserem);
			signalFade = menu.findItem(R.id.bar_signal_fade);
			signalRemFade = menu.findItem(R.id.bar_signal_faderem);
			mode = (SelectModeSpinner) menu.findItem(R.id.bar_mode)
					.getActionView();
			timer = new TimerView(propeler);
		} else {
			menuInflater.inflate(R.menu.menu_newui_disconnected, menu);
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case BATTERY:
			updateBatteryInfo(drone);
			break;
		case GPS_FIX:
			updateGpsInfo(drone);
			break;
		case RADIO:
			updateRadioInfo(drone);
			break;
		case STATE:
			updateFlightStateInfo(drone);
			break;
		case HOME:
			updateHomeInfo(drone);
			break;
		default:
			break;
		}
		if(mode!=null){
			mode.onDroneEvent(event,drone);
		}
	}

	private void updateBatteryInfo(Drone drone) {
		battery.setTitle(String.format(" %2.1fv, %2.0f%% ",
				drone.battery.getBattVolt(), drone.battery.getBattRemain()));
	}

	private void updateGpsInfo(Drone drone) {
		gps.setTitle(String.format(" %d, %s", drone.GPS.getSatCount(),
				drone.GPS.getFixType()));
	}

	private void updateRadioInfo(Drone drone) {
		signal.setTitle(String.format("%d%%", drone.radio.getSignalStrength()));
		signalRSSI.setTitle(String.format("RSSI %2.0f dB",
				drone.radio.getRssi()));
		signalRemRSSI.setTitle(String.format("RemRSSI %2.0f dB",
				drone.radio.getRemRssi()));
		signalNoise.setTitle(String.format("Noise %2.0f dB",
				drone.radio.getNoise()));
		signalRemNoise.setTitle(String.format("RemNoise %2.0f dB",
				drone.radio.getRemNoise()));
		signalFade.setTitle(String.format("Fade %2.0f dB",
				drone.radio.getFadeMargin()));
		signalRemFade.setTitle(String.format("RemFade %2.0f dB",
				drone.radio.getRemFadeMargin()));
	}

	public void updateHomeInfo(Drone drone) {
		home.setTitle(drone.home.getDroneDistanceToHome().toString());
	}

	public void updateFlightStateInfo(Drone drone) {
		if (drone.state.isFlying()) {
			timer.reStart();
		} else {
			timer.stop();
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

	public void setupModeSpinner(Context context) {
		if (mode != null) {
			mode.buildSpinner(context, drone);
		}
	}
}
