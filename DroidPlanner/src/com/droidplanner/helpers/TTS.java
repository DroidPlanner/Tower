package com.droidplanner.helpers;

import java.util.Locale;

import com.MAVLink.Messages.ApmModes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class TTS implements OnInitListener {
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
	
	public void speakGpsMode(int fix) {
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
	
	public void speakArmedState(boolean armed) {
		if (armed) {
			speak("Armed");					
		}else{
			speak("Disarmed");
		}
	}

	public void speakMode(ApmModes mode) {
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
	
	
	public void batteryDischargeNotification(double battRemain) {
		if (lastBatteryDischargeNotification != (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT)) {
			lastBatteryDischargeNotification = (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT);
			speak("Battery at" + (int) battRemain + "%");
		}
	}
}
