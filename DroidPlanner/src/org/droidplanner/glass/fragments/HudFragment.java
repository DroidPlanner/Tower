package org.droidplanner.glass.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.droidplanner.R;
import org.droidplanner.activities.helpers.SuperActivity;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneEvents;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.drone.variables.Altitude;
import org.droidplanner.drone.variables.Battery;
import org.droidplanner.drone.variables.GPS;
import org.droidplanner.drone.variables.Orientation;
import org.droidplanner.drone.variables.Speed;
import org.droidplanner.glass.utils.hud.HUD;

public class HudFragment extends Fragment implements DroneInterfaces.OnDroneListener{

	private HUD hudWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_attitude_indicator, container, false);
		hudWidget = (HUD) view.findViewById(R.id.hudWidget);
		return view;
	}

    @Override
    public void onStart(){
        super.onStart();

        final DroneEvents droneEvents = ((SuperActivity) getActivity()).app.drone.events;
        droneEvents.addDroneListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        final DroneEvents droneEvents = ((SuperActivity) getActivity()).app.drone.events;
        droneEvents.removeDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch(event){
            case ARMING:
            case MODE:
                hudWidget.updateDroneState(drone.state);
                break;

            case BATTERY:
                hudWidget.updateBatteryInfo(drone.battery);
                break;

            case CONNECTED:
                //Enable the hud view
                hudWidget.setEnabled(true);
                break;

            case DISCONNECTED:
                //Disable the hud view
                hudWidget.setEnabled(false);
                break;

            case GPS:
            case GPS_COUNT:
            case GPS_FIX:
                hudWidget.updateGpsInfo(drone.GPS);
                break;

            case ORIENTATION:
                //Update yaw, pitch, and roll
                hudWidget.updateOrientation(drone.orientation);
                break;

            case SPEED:
                hudWidget.updateAltitudeAndSpeed(drone.altitude, drone.speed);
                break;

            case TYPE:
                hudWidget.setDroneType(drone.type.getType());
                break;
        }
    }
}
