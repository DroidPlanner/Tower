package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.widgets.AttitudeIndicator;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TelemetryFragment extends Fragment implements OnDroneListener {

	private AttitudeIndicator attitudeIndicator;
	private Drone drone;
	private TextView roll;
	private TextView yaw;
	private TextView pitch;
	private TextView groundSpeed;
	private TextView airSpeed;
	private TextView climbRate;
	private TextView altitude;
	private TextView targetAltitude;
	private boolean headingModeFPV;
    private TextView lblController;
    private RelativeLayout rcLayout;
    private TextView lblControllerRight;

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
		
		rcLayout = (RelativeLayout) view.findViewById(R.id.rc_layout);
		lblController = (TextView) view.findViewById(R.id.lblRCValue);
		lblControllerRight = (TextView) view.findViewById(R.id.lblRCValueRight);

		drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.addDroneListener(this);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()
				.getApplicationContext());
		headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.removeDroneListener(this);
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
	
    public void setControllerStatusVisible(boolean visible) {
        if(rcLayout == null)
            return;
        
        if (visible) {
            if(lblController.getText().length() < 5) //Set default text
            {
                float[] channels = new float[8];
                channels[RCConstants.THROTTLE] = 0;
                channels[RCConstants.AILERON] = 0;
                channels[RCConstants.ELEVATOR] = 0;
                channels[RCConstants.RUDDER] = 0;
                channels[RCConstants.RC5] = 0;
                channels[RCConstants.RC6] = 0;
                channels[RCConstants.RC7] = 0;
                channels[RCConstants.RC8] = 0;
                updateControllerStatus(channels);
            }
            rcLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            rcLayout.setVisibility(View.GONE);
        }
    }
	
	public void updateControllerStatus(float[] channels) {
	    
	    String leftText = "";
	    for(int x = 0; x < RCConstants.rchannels.length; x += 2) {
	        leftText += RCConstants.ShortRChannelsTitle[x] + "\n";
	        leftText += Math.round(channels[x]) + "\n";
	    }
	    leftText += "";
	    
	       String rightText = "";
        for(int x = 1; x < RCConstants.rchannels.length; x += 2) {
            rightText += RCConstants.ShortRChannelsTitle[x] + "\n";
            rightText += Math.round(channels[x]) + "\n";
        }
        
        lblController.setText(leftText);
	    lblControllerRight.setText(rightText);
	}
}
