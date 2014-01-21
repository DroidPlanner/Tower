package org.droidplanner.helpers;

import java.util.Locale;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.variables.Calibration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.MAVLink.Messages.ApmModes;

public class TTS implements OnInitListener, OnDroneListener {
	private static final double BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT = 10;

	TextToSpeech tts;
	private SharedPreferences prefs;
	private int lastBatteryDischargeNotification;

	public TTS(Context context) {
		tts = new TextToSpeech(context, this);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onInit(int status) {
		tts.setLanguage(Locale.US);
	}

	public void speak(String string) {
		if (tts != null) {
			if (shouldEnableTTS()) {
				tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	private boolean shouldEnableTTS() {
		return prefs.getBoolean("pref_enable_tts", false);
	}


	/**
	 * Warn the user if needed via the TTS module
	 */
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (tts != null) {
			switch (event) {
			case ARMING:
				speakArmedState(drone.state.isArmed());
				break;
			case BATTERY:
				batteryDischargeNotification(drone.battery
						.getBattRemain());
				break;
			case MODE:
				speakMode(drone.state.getMode());
				break;
			case GPS_FIX:
				speakGpsMode(drone.GPS.getFixTypeNumeric());
				break;
			case MISSION_RECEIVED:
				speak("Waypoints received");
				break;
			case HEARTBEAT_FIRST:
				speak("Connected");
				break;
			case HEARTBEAT_TIMEOUT:
				if (!Calibration.isCalibrating()) {
					speak("Data link lost, check connection.");
				}
				break;
			case HEARTBEAT_RESTORED:
				speak("Data link restored");
				break;
			default:
				break;
			}
		}
	}

	private void speakArmedState(boolean armed) {
		if (armed) {
			speak("Armed");
		} else {
			speak("Disarmed");
		}
	}

	private void batteryDischargeNotification(double battRemain) {
		if (lastBatteryDischargeNotification != (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT)) {
			lastBatteryDischargeNotification = (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT);
			speak("Battery at" + (int) battRemain + "%");
		}
	}

	private void speakMode(ApmModes mode) {
		String modeString = "Mode ";
		switch (mode) {
		case FIXED_WING_FLY_BY_WIRE_A:
			modeString += "Fly by wire A";
			break;
		case FIXED_WING_FLY_BY_WIRE_B:
			modeString += "Fly by wire B";
			break;
		case ROTOR_ACRO:
			modeString += "Acrobatic";
			break;
		case ROTOR_ALT_HOLD:
			modeString += "Altitude hold";
			break;
		case ROTOR_POSITION:
			modeString += "Position hold";
			break;
		case FIXED_WING_RTL:
		case ROTOR_RTL:
			modeString += "Return to home";
			break;
		default:
			modeString += mode.getName();
			break;
		}
		speak(modeString);
	}

	private void speakGpsMode(int fix) {
		switch (fix) {
		case 2:
			speak("GPS 2D Lock");
			break;
		case 3:
			speak("GPS 3D Lock");
			break;
		default:
			speak("Lost GPS Lock");
			break;
		}
	}
}
