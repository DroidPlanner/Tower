package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.helpers.ApiInterface;
import org.droidplanner.android.widgets.AttitudeIndicator;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TelemetryFragment extends Fragment implements OnDroneListener, ApiInterface.Subscriber {

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

    private DroidPlannerApi dpApi;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof ApiInterface.Provider)) {
            throw new IllegalStateException("Parent activity must be an instance of "
                    + ApiInterface.Provider.class.getName());
        }
    }

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

        final Activity activity = getActivity();

        ApiInterface.Provider apiProvider = (ApiInterface.Provider) activity;
        DroidPlannerApi api = apiProvider == null ? null : apiProvider.getApi();
        if(api != null) {
            onApiConnected(api);
        }

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity
				.getApplicationContext());
		headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
	}

	@Override
	public void onStop() {
		super.onStop();
		onApiDisconnected();
	}

    @Override
    public void onApiConnected(DroidPlannerApi api) {
        dpApi = api;
        api.addDroneListener(this);
    }

    @Override
    public void onApiDisconnected() {
        if(dpApi != null){
            dpApi.removeDroneListener(this);
        }
    }

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case NAVIGATION:
			break;
		case ATTITUDE:
			onOrientationUpdate(drone);
			break;
		case SPEED:
			onSpeedAltitudeAndClimbRateUpdate(drone);
			break;
		default:
			break;
		}

	}

	public void onOrientationUpdate(Drone drone) {
		float r = (float) drone.getOrientation().getRoll();
		float p = (float) drone.getOrientation().getPitch();
		float y = (float) drone.getOrientation().getYaw();

		if (!headingModeFPV & y < 0) {
			y = 360 + y;
		}

		attitudeIndicator.setAttitude(r, p, y);

		roll.setText(String.format("%3.0f\u00B0", r));
		pitch.setText(String.format("%3.0f\u00B0", p));
		yaw.setText(String.format("%3.0f\u00B0", y));

	}

	public void onSpeedAltitudeAndClimbRateUpdate(Drone drone) {
		airSpeed.setText(String.format("%3.1f", drone.getSpeed().getAirSpeed()
				.valueInMetersPerSecond()));
		groundSpeed.setText(String.format("%3.1f", drone.getSpeed().getGroundSpeed()
				.valueInMetersPerSecond()));
		climbRate.setText(String.format("%3.1f", drone.getSpeed().getVerticalSpeed()
				.valueInMetersPerSecond()));
		double alt = drone.getAltitude().getAltitude();
		double targetAlt = drone.getAltitude().getTargetAltitude();
		altitude.setText(String.format("%3.1f", alt));
		targetAltitude.setText(String.format("%3.1f", targetAlt));
	}

}
