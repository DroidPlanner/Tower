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
	public void onDroneEvent(DroneEventsType event) {
		mode.onDroneEvent(event);
		switch (event) {
		case BATTERY:
			updateBatteryInfo();
			break;
		case GPS:
			updateGpsInfo();
			break;
		case RADIO:
			updateRadioInfo();
			break;
		case STATE:
			updateFlightStateInfo();
			break;
		case HOME:
			updateHomeInfo();
			break;
		default:
			break;
		}
	}

	private void updateBatteryInfo() {
		battery.setTitle(String.format(" %2.1fv, %2.0f%% ",
				drone.battery.getBattVolt(), drone.battery.getBattRemain()));
	}

	private void updateGpsInfo() {
		gps.setTitle(String.format(" %d, %s", drone.GPS.getSatCount(),
				drone.GPS.getFixType()));
	}

	private void updateRadioInfo() {
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

	public void updateHomeInfo() {
		home.setTitle(drone.home.getDroneDistanceToHome().toString());
	}

	public void updateFlightStateInfo() {
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
