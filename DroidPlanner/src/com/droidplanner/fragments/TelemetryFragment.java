package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.widgets.newHUD.newHUD;

public class TelemetryFragment extends Fragment implements HudUpdatedListner {

	private newHUD hud;
	private Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_telemetry, container,
				false);
		hud = (newHUD) view.findViewById(R.id.hudView);

		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		drone.setHudListner(this);
		return view;
	}

	@Override
	public void onDroneUpdate() {
		hud.setAttitude((float) drone.orientation.getRoll(),
				(float) drone.orientation.getPitch(),
				(float) drone.orientation.getYaw());

	}

}
