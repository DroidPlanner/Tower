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

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Speed;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.widgets.AttitudeIndicator;

public class TelemetryFragment extends ApiListenerFragment {

	private final static IntentFilter eventFilter = new IntentFilter();
	static {
		eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
		eventFilter.addAction(AttributeEvent.SPEED_UPDATED);
	}

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final Drone drone = getDrone();

            switch (action) {
                case AttributeEvent.ATTITUDE_UPDATED:
                    final Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
                    onOrientationUpdate(attitude);
                    break;

                case AttributeEvent.SPEED_UPDATED:
                    final Speed droneSpeed = drone.getAttribute(AttributeType.SPEED);
                    onSpeedUpdate(droneSpeed);

                    final Altitude droneAltitude = drone.getAttribute(AttributeType.ALTITUDE);
                    onAltitudeUpdate(droneAltitude);
                    break;
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
        if(droneAttitude == null)
            return;

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

	private void onSpeedUpdate(Speed speed) {
        if(speed != null) {
            airSpeed.setText(String.format("%3.1f", speed.getAirSpeed()));
            groundSpeed.setText(String.format("%3.1f", speed.getGroundSpeed()));
            climbRate.setText(String.format("%3.1f", speed.getVerticalSpeed()));
        }
	}

    private void onAltitudeUpdate(Altitude altitude){
        if(altitude != null) {
            double alt = altitude.getAltitude();
            double targetAlt = altitude.getTargetAltitude();

            this.altitude.setText(String.format("%3.1f", alt));
            targetAltitude.setText(String.format("%3.1f", targetAlt));
        }
    }

}
