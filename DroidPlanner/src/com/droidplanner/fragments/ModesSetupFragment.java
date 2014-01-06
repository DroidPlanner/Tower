package com.droidplanner.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;

public class ModesSetupFragment extends Fragment implements OnDroneListner {

	// Extreme RC update rate in this screen
	//private static final int RC_MSG_RATE = 50;

	private Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_modes, container,false);

		return view;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
        drone.events.addDroneListener(this);
    }

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case RC_IN:
			int[] data = drone.RC.in;
			break;
		case RC_OUT:
			break;
		default:
			break;
		}		
	}

}
