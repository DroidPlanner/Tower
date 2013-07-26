package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.R;
import com.droidplanner.activitys.SuperActivity;
import com.droidplanner.widgets.HUD.HUD;

public class HudFragment extends Fragment {

	private HUD hudWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hud_fragment, container, false);
		hudWidget = (HUD) view.findViewById(R.id.hudWidget);
		hudWidget.setDrone(((SuperActivity) getActivity()).app.drone);
		hudWidget.onDroneUpdate();
		return view;
	}

}
