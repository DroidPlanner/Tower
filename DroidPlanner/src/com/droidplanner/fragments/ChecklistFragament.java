package com.droidplanner.fragments;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.Drone;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class ChecklistFragament extends Fragment{
	private Context context;
	private Drone drone;
	private View view;
	private ExpandableListView expListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_checklist, null);
		expListView = (ExpandableListView) view.findViewById(R.id.expListView);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        this.context = activity;
        this.drone = ((SuperActivity) activity).drone;
	}


	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public ChecklistFragament() {
		// TODO Auto-generated constructor stub
	}

}
