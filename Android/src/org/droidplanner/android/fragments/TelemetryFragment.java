package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Speed;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.widgets.AttitudeIndicator;

public class TelemetryFragment extends ApiListenerFragment {

	private final static IntentFilter eventFilter = new IntentFilter();
	static {
		eventFilter.addAction(Event.EVENT_ATTITUDE);
		eventFilter.addAction(Event.EVENT_SPEED);
	}

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final DroneApi droneApi = getDroneApi();
			if (Event.EVENT_ATTITUDE.equals(action)) {
				onOrientationUpdate(droneApi.getAttitude());
			} else if (Event.EVENT_SPEED.equals(action)) {
				onSpeedAltitudeAndClimbRateUpdate(droneApi.getSpeed(), droneApi.getAltitude());
			}
		}
	};

	private AttitudeIndicator attitudeIndicator;
	private TextView roll;
	private TextView yaw;
	private TextView pitch;
	private TextView groundSpeed;
	private TextView airSpeed;
	private TextView climbRate;
	private TextView altitude;
	private TextView targetAltitude;
	private boolean headingModeFPV;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_telemetry, container, false);
		attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

		roll = (TextView) view.findViewById(R.id.rollValueText);
		yaw = (TextView) view.findViewById(R.id.yawValueText);
		pitch = (TextView) view.findViewById(R.id.pitchValueText);

		groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue);
		airSpeed = (TextView) view.findViewById(R.id.airSpeedValue);
		climbRate = (TextView) view.findViewById(R.id.climbRateValue);
		altitude = (TextView) view.findViewById(R.id.altitudeValue);
		targetAltitude = (TextView) view.findViewById(R.id.targetAltitudeValue);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()
				.getApplicationContext());
		headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
	}

	@Override
	public void onApiConnected() {
		getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
	}

	@Override
	public void onApiDisconnected() {
		getBroadcastManager().unregisterReceiver(eventReceiver);
	}

	public void onOrientationUpdate(Attitude droneAttitude) {
		float r = (float) droneAttitude.getRoll();
		float p = (float) droneAttitude.getPitch();
		float y = (float) droneAttitude.getYaw();

		if (!headingModeFPV & y < 0) {
			y = 360 + y;
		}

		attitudeIndicator.setAttitude(r, p, y);

		roll.setText(String.format("%3.0f\u00B0", r));
		pitch.setText(String.format("%3.0f\u00B0", p));
		yaw.setText(String.format("%3.0f\u00B0", y));

	}

	public void onSpeedAltitudeAndClimbRateUpdate(Speed speed, Altitude altitude) {
		airSpeed.setText(String.format("%3.1f", speed.getAirSpeed()));
		groundSpeed.setText(String.format("%3.1f", speed.getGroundSpeed()));
		climbRate.setText(String.format("%3.1f", speed.getVerticalSpeed()));
		double alt = altitude.getAltitude();
		double targetAlt = altitude.getTargetAltitude();

		this.altitude.setText(String.format("%3.1f", alt));
		targetAltitude.setText(String.format("%3.1f", targetAlt));
	}

}
