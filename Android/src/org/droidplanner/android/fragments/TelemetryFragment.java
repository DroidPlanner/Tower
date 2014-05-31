package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.fragments.helpers.GenericDialogFragment;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.android.widgets.AttitudeIndicator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TelemetryFragment extends Fragment implements OnDroneListener {

    /**
     * This is the period for the flight time update.
     */
    private final static long AIR_TIMER_PERIOD = 1000l; // 1 second

	private AttitudeIndicator attitudeIndicator;
	private Drone drone;
	private TextView roll;
	private TextView yaw;
	private TextView pitch;
	private TextView groundSpeed;
	private TextView airSpeed;
	private TextView climbRate;
	private TextView altitude;
	private boolean headingModeFPV;

    /*
    Air time view and textview.
     */
    private TextView mAirTime;

    private final Handler mAirTimeHandler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_telemetry, container, false);

        drone = ((DroidPlannerApp) getActivity().getApplication()).drone;

		attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

		roll = (TextView) view.findViewById(R.id.rollValueText);
		yaw = (TextView) view.findViewById(R.id.yawValueText);
		pitch = (TextView) view.findViewById(R.id.pitchValueText);

		groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue);
		airSpeed = (TextView) view.findViewById(R.id.airSpeedValue);
		climbRate = (TextView) view.findViewById(R.id.climbRateValue);
		altitude = (TextView) view.findViewById(R.id.altitudeValue);

        mAirTime = (TextView) view.findViewById(R.id.telemetry_air_time);
        mAirTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Runnable resetTimerCb = new Runnable() {
                    @Override
                    public void run() {
                        if(drone != null) {
                            drone.state.resetFlightTimer();
                        }
                        else{
                            mAirTime.setText("--:--");
                        }
                    }
                };

                final GenericDialogFragment confirmDialog = GenericDialogFragment.newInstance
                        ("Air Timer", "Reset timer?", "Reset", resetTimerCb, "Cancel", null);
                confirmDialog.show(getChildFragmentManager(), "Timer confirmation dialog");
                return true;
            }
        });

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity().getApplicationContext());
		headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, final Drone drone) {
		switch (event) {
		case NAVIGATION:
			break;
		case ORIENTATION:
			onOrientationUpdate(drone);
			break;
		case SPEED:
			onSpeedAltitudeAndClimbRateUpdate(drone);
			break;

            case STATE:
                mAirTimeHandler.removeCallbacksAndMessages(null);
                if(drone != null){
                    final Runnable airTimeUpdater = new Runnable() {
                        @Override
                        public void run() {
                            if(mAirTime != null){
                                long timeInSeconds = drone.state.getFlightTime();
                                long minutes = timeInSeconds / 60;
                                long seconds = timeInSeconds % 60;

                                mAirTime.setText(String.format("%02d:%02d", minutes, seconds));
                            }

                            mAirTimeHandler.postDelayed(this, AIR_TIMER_PERIOD);
                        }
                    };
                    airTimeUpdater.run();
                }
                else{
                    mAirTime.setText("--:--");
                }
                break;

		default:
			break;
		}

	}

	public void onOrientationUpdate(Drone drone) {
		float r = (float) drone.orientation.getRoll();
		float p = (float) drone.orientation.getPitch();
		float y = (float) drone.orientation.getYaw();

		if (!headingModeFPV & y < 0) {
			y = 360 + y;
		}

		attitudeIndicator.setAttitude(r, p, y);

		roll.setText(String.format("%3.0f\u00B0", r));
		pitch.setText(String.format("%3.0f\u00B0", p));
		yaw.setText(String.format("%3.0f\u00B0", y));

	}

	public void onSpeedAltitudeAndClimbRateUpdate(Drone drone) {
		airSpeed.setText(String.format("%3.1f", drone.speed.getAirSpeed()));
		groundSpeed.setText(String.format("%3.1f", drone.speed.getGroundSpeed()));
		climbRate.setText(String.format("%3.1f", drone.speed.getVerticalSpeed()));
		double alt = drone.altitude.getAltitude();
		altitude.setText(String.format("%3.1f", alt));
	}

}
