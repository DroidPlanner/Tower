package com.droidplanner.activitys.helpers;

import android.content.Context;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
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
			findViews(menu);
		} else {
			menuInflater.inflate(R.menu.menu_newui_disconnected, menu);
		}
	}

	private void findViews(Menu menu) {
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
		mode = (SelectModeSpinner) menu.findItem(R.id.bar_mode).getActionView();
		timer = new TimerView(propeler, drone);
	}

	public void forceViewsUpdate() {
		onDroneEvent(DroneEventsType.BATTERY, drone);
		onDroneEvent(DroneEventsType.GPS_FIX, drone);
		onDroneEvent(DroneEventsType.RADIO, drone);
		onDroneEvent(DroneEventsType.STATE, drone);
		onDroneEvent(DroneEventsType.HOME, drone);
		onDroneEvent(DroneEventsType.MODE, drone);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		try {
			switch (event) {
			case BATTERY:
				updateBatteryInfo(drone);
				break;
			case GPS_FIX:
			case GPS_COUNT:
				updateGpsInfo(drone);
				break;
			case RADIO:
				updateRadioInfo(drone);
				break;
			case HOME:
				updateHomeInfo(drone);
				break;
			default:
				break;
			}
			mode.onDroneEvent(event, drone);
		} catch (NullPointerException e) {
			// Can fail saftly with null pointer if the layout's have not been
			// inflated yet
		}
	}

	private void updateBatteryInfo(Drone drone) {
		SpannableString text = new SpannableString(String.format("   Battery\n  %2.1fv, %2.0f%% ",drone.battery.getBattVolt(), drone.battery.getBattRemain()));
		text.setSpan(new RelativeSizeSpan(.8f), 0, 10, 0);
		text.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		battery.setTitle(text);
	}

	private void updateGpsInfo(Drone drone) {
		SpannableString text = new SpannableString(String.format("   Satellite\n  %d, %s", drone.GPS.getSatCount(), drone.GPS.getFixType()));
		text.setSpan(new RelativeSizeSpan(.8f), 0, 13, 0);
		text.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		gps.setTitle(text);
	}

	private void updateRadioInfo(Drone drone) {
		SpannableString text = new SpannableString(String.format("   Signal\n  %d%%", drone.radio.getSignalStrength()));
		text.setSpan(new RelativeSizeSpan(.8f), 0, 9, 0);
		text.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		signal.setTitle(text);

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
		SpannableString text = new SpannableString(String.format("   Home\n  %s", drone.home.getDroneDistanceToHome().toString()));
		text.setSpan(new RelativeSizeSpan(.8f), 0, 7, 0);
		text.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		home.setTitle(text);
	}

	public void onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.bar_timer_reset:
			drone.state.resetFlightTimer();
			break;
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
