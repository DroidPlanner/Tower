package org.droidplanner.android.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.event.Extra;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;

import org.droidplanner.R;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements DroidPlanner audible notifications.
 */
public class TTSNotificationProvider implements OnInitListener,
		NotificationHandler.NotificationProvider {

	private static final String CLAZZ_NAME = TTSNotificationProvider.class.getName();
	private static final String TAG = TTSNotificationProvider.class.getSimpleName();

	private static final double BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT = 10;

	/**
	 * Utterance id for the periodic status speech.
	 */
	private static final String PERIODIC_STATUS_UTTERANCE_ID = "periodic_status_utterance";

	/**
	 * Action used for message to be delivered by the tts speech engine.
	 */
	public static final String ACTION_SPEAK_MESSAGE = CLAZZ_NAME + ".ACTION_SPEAK_MESSAGE";
	public static final String EXTRA_MESSAGE_TO_SPEAK = "extra_message_to_speak";

	private final AtomicBoolean mIsPeriodicStatusStarted = new AtomicBoolean(false);
	/**
	 * Listens for updates to the status interval.
	 */
	private final BroadcastReceiver mSpeechIntervalUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (SettingsFragment.ACTION_UPDATED_STATUS_PERIOD.equals(action)) {
				scheduleWatchdog();
			} else if (ACTION_SPEAK_MESSAGE.equals(action)) {
				String msg = intent.getStringExtra(EXTRA_MESSAGE_TO_SPEAK);
				if (msg != null) {
					speak(msg);
				}
			}
		}
	};

	/**
	 * Monitors speech completion.
	 */
	private final TextToSpeech.OnUtteranceCompletedListener mSpeechCompleteListener = new TextToSpeech.OnUtteranceCompletedListener() {
		@Override
		public void onUtteranceCompleted(String utteranceId) {
			if (PERIODIC_STATUS_UTTERANCE_ID.equals(utteranceId)) {
				mIsPeriodicStatusStarted.set(false);
			}
		}
	};

	/**
	 * Stored the parameters to be passed to the tts `speak(...)` method.
	 */
	private final HashMap<String, String> mTtsParams = new HashMap<String, String>();

	private TextToSpeech tts;
	private int lastBatteryDischargeNotification;

	private final Context context;
	private final DroidPlannerPrefs mAppPrefs;
	private final Handler handler = new Handler();
	private int statusInterval;

	private class Watchdog implements Runnable {

		private final StringBuilder mMessageBuilder = new StringBuilder();
		private Drone drone;

		public void run() {
			handler.removeCallbacks(watchdogCallback);
			if (drone != null && drone.isConnected() && drone.getState().isArmed()) {
				speakPeriodic(drone);
			}

			if (statusInterval != 0) {
				handler.postDelayed(watchdogCallback, statusInterval * 1000);
			}
		}

		private void speakPeriodic(Drone drone) {
			// Drop the message if the previous one is not done yet.
			if (mIsPeriodicStatusStarted.compareAndSet(false, true)) {
				final SparseBooleanArray speechPrefs = mAppPrefs.getPeriodicSpeechPrefs();

				mMessageBuilder.setLength(0);
				if (speechPrefs.get(R.string.pref_tts_periodic_bat_volt_key)) {
					mMessageBuilder.append(String.format("battery %2.1f volts. ", drone
							.getBattery().getBatteryVoltage()));
				}

				if (speechPrefs.get(R.string.pref_tts_periodic_alt_key)) {
					mMessageBuilder.append("altitude, " + (int) (drone.getAltitude().getAltitude())
							+ " meters. ");
				}

				if (speechPrefs.get(R.string.pref_tts_periodic_airspeed_key)) {
					mMessageBuilder.append("airspeed, " + (int) (drone.getSpeed().getAirSpeed())
							+ " meters per second. ");
				}

				if (speechPrefs.get(R.string.pref_tts_periodic_rssi_key)) {
					mMessageBuilder.append("r s s i, " + (int) drone.getSignal().getRssi()
							+ " decibels");
				}

				speak(mMessageBuilder.toString(), true, PERIODIC_STATUS_UTTERANCE_ID);
			}
		}

		public void setDrone(Drone drone) {
			this.drone = drone;
		}
	}

	public final Watchdog watchdogCallback = new Watchdog();

	private final Drone drone;

	TTSNotificationProvider(Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		tts = new TextToSpeech(context, this);
		mAppPrefs = new DroidPlannerPrefs(context);

        LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
	}

	private void scheduleWatchdog() {
		handler.removeCallbacks(watchdogCallback);
		statusInterval = mAppPrefs.getSpokenStatusInterval();
		if (statusInterval != 0) {
			handler.postDelayed(watchdogCallback, statusInterval * 1000);
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// TODO: check if the language is available
			Locale ttsLanguage;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				ttsLanguage = tts.getDefaultLanguage();
			} else {
				ttsLanguage = tts.getLanguage();
			}

			if (ttsLanguage == null) {
				ttsLanguage = Locale.US;
			}

			int supportStatus = tts.setLanguage(ttsLanguage);
			switch (supportStatus) {
			case TextToSpeech.LANG_MISSING_DATA:
			case TextToSpeech.LANG_NOT_SUPPORTED:
				tts.shutdown();
				tts = null;

				Log.e(TAG, "TTS Language data is not available.");
				Toast.makeText(context, "Unable to set 'Text to Speech' language!",
						Toast.LENGTH_LONG).show();
				break;
			}

			if (tts != null) {
				tts.setOnUtteranceCompletedListener(mSpeechCompleteListener);

				// Register the broadcast receiver
				final IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(ACTION_SPEAK_MESSAGE);
				intentFilter.addAction(SettingsFragment.ACTION_UPDATED_STATUS_PERIOD);

				LocalBroadcastManager.getInstance(context).registerReceiver(
						mSpeechIntervalUpdateReceiver, intentFilter);
			}
		} else {
			// Notify the user that the tts engine is not available.
			Log.e(TAG, "TextToSpeech initialization failed.");
			Toast.makeText(
					context,
					"Please make sure 'Text to Speech' is enabled in the "
							+ "system accessibility settings.", Toast.LENGTH_LONG).show();
		}
	}

	private void speak(String string) {
		speak(string, false, null);
	}

	private void speak(String string, boolean append, String utteranceId) {
		if (tts != null) {
			if (shouldEnableTTS()) {
				final int queueType = append ? TextToSpeech.QUEUE_ADD : TextToSpeech.QUEUE_FLUSH;

				mTtsParams.clear();
				if (utteranceId != null) {
					mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
				}

				tts.speak(string, queueType, mTtsParams);
			}
		}
	}

	private boolean shouldEnableTTS() {
		return mAppPrefs.prefs.getBoolean("pref_enable_tts", false);
	}

	private final static IntentFilter eventFilter = new IntentFilter();
	static {
		eventFilter.addAction(Event.EVENT_ARMING);
		eventFilter.addAction(Event.EVENT_BATTERY);
		eventFilter.addAction(Event.EVENT_VEHICLE_MODE);
		eventFilter.addAction(Event.EVENT_MISSION_SENT);
		eventFilter.addAction(Event.EVENT_GPS_FIX);
		eventFilter.addAction(Event.EVENT_MISSION_RECEIVED);
		eventFilter.addAction(Event.EVENT_HEARTBEAT_FIRST);
		eventFilter.addAction(Event.EVENT_HEARTBEAT_TIMEOUT);
		eventFilter.addAction(Event.EVENT_HEARTBEAT_RESTORED);
		eventFilter.addAction(Event.EVENT_DISCONNECTED);
		eventFilter.addAction(Event.EVENT_MISSION_ITEM_UPDATE);
		eventFilter.addAction(Event.EVENT_FOLLOW_START);
		eventFilter.addAction(Event.EVENT_AUTOPILOT_FAILSAFE);
		eventFilter.addAction(Event.EVENT_WARNING_400FT_EXCEEDED);
		eventFilter.addAction(Event.EVENT_WARNING_SIGNAL_WEAK);
		eventFilter.addAction(Event.EVENT_WARNING_NO_GPS);

	}

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (tts == null)
				return;

			final String action = intent.getAction();
            State droneState = drone.getState();

			if (Event.EVENT_ARMING.equals(action)) {
                if(droneState != null)
				    speakArmedState(droneState.isArmed());
			} else if (Event.EVENT_BATTERY.equals(action)) {
                Battery droneBattery = drone.getBattery();
                if(droneBattery != null)
				    batteryDischargeNotification(droneBattery.getBatteryRemain());
			} else if (Event.EVENT_VEHICLE_MODE.equals(action)) {
                if(droneState != null)
				    speakMode(droneState.getVehicleMode());
			} else if (Event.EVENT_MISSION_SENT.equals(action)) {
				Toast.makeText(context, "Waypoints sent", Toast.LENGTH_SHORT).show();
				speak("Waypoints saved to Drone");
			} else if (Event.EVENT_GPS_FIX.equals(action)) {
                Gps droneGps = drone.getGps();
                if(droneGps != null)
				    speakGpsMode(droneGps.getFixType());
			} else if (Event.EVENT_MISSION_RECEIVED.equals(action)) {
				Toast.makeText(context, "Waypoints received from Drone", Toast.LENGTH_SHORT).show();
				speak("Waypoints received");
			} else if (Event.EVENT_HEARTBEAT_FIRST.equals(action)) {
				watchdogCallback.setDrone(drone);
				scheduleWatchdog();
				speak("Connected");
			} else if (Event.EVENT_HEARTBEAT_TIMEOUT.equals(action)) {
				if (droneState != null && !droneState.isCalibrating()
						&& mAppPrefs.getWarningOnLostOrRestoredSignal()) {
					speak("Data link lost, check connection.");
					handler.removeCallbacks(watchdogCallback);
				}
			}
            else if(Event.EVENT_HEARTBEAT_RESTORED.equals(action)){
                watchdogCallback.setDrone(drone);
                scheduleWatchdog();
                if (mAppPrefs.getWarningOnLostOrRestoredSignal()) {
                    speak("Data link restored");
                }
            }
            else if(Event.EVENT_DISCONNECTED.equals(action)){
                handler.removeCallbacks(watchdogCallback);
            }
            else if(Event.EVENT_MISSION_ITEM_UPDATE.equals(action)){
                int currentWaypoint = intent.getIntExtra(Extra.EXTRA_MISSION_CURRENT_WAYPOINT, 0);
                speak("Going for waypoint " + currentWaypoint);
            }
            else if(Event.EVENT_FOLLOW_START.equals(action)){
                speak("Following");
            }
            else if(Event.EVENT_WARNING_400FT_EXCEEDED.equals(action)){
                if (mAppPrefs.getWarningOn400ftExceeded()) {
                    speak("warning, 400 feet exceeded");
                }
            }
            else if(Event.EVENT_AUTOPILOT_FAILSAFE.equals(action)){
                String warning = intent.getStringExtra(Extra.EXTRA_AUTOPILOT_FAILSAFE_MESSAGE);
                if (!TextUtils.isEmpty(warning) && mAppPrefs.getWarningOnAutopilotWarning()) {
                    speak(warning);
                }
            }
            else if(Event.EVENT_WARNING_SIGNAL_WEAK.equals(action)){
                if (mAppPrefs.getWarningOnLowSignalStrength()) {
                    speak("Warning, weak signal");
                }
            }
            else if(Event.EVENT_WARNING_NO_GPS.equals(action)){
                speak("Error, no gps lock yet");
            }
		}
	};

	private void speakArmedState(boolean armed) {
		if (armed) {
			speak("Armed");
		} else {
			speak("Disarmed");
		}
	}

	private void batteryDischargeNotification(double battRemain) {
		if (lastBatteryDischargeNotification > (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT)
				|| lastBatteryDischargeNotification + 1 < (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT)) {
			lastBatteryDischargeNotification = (int) ((battRemain - 1) / BATTERY_DISCHARGE_NOTIFICATION_EVERY_PERCENT);
			speak("Battery at" + (int) battRemain + "%");
		}
	}

	private void speakMode(VehicleMode mode) {
		String modeString = "Mode ";
		switch (mode) {
		case PLANE_FLY_BY_WIRE_A:
			modeString += "Fly by wire A";
			break;
		case PLANE_FLY_BY_WIRE_B:
			modeString += "Fly by wire B";
			break;
		case COPTER_ACRO:
			modeString += "Acrobatic";
			break;
		case COPTER_ALT_HOLD:
			modeString += "Altitude hold";
			break;
		case COPTER_POSHOLD:
			modeString += "Position hold";
			break;
		case PLANE_RTL:
		case COPTER_RTL:
			modeString += "Return to launch";
			break;
		default:
			modeString += mode.getLabel();
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

	@Override
	public void onTerminate() {
		if (tts != null) {
			tts.shutdown();
		}

        LocalBroadcastManager.getInstance(context).unregisterReceiver(eventReceiver);
	}
}
