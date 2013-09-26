package com.droidplanner.dialogs.mission;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionSetJump extends DialogMission implements
		OnTextSeekBarChangedListner, OnItemSelectedListener  {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText repeatSeekBar;
	private Spinner jumpToSpinner;
	private ArrayAdapter<String> jumpToAdapter;
	private ArrayList<String> jumpToList;
	private List<waypoint> wplist;
	private DroidPlannerApp app;
	
	@Override
	protected int getResource() {
		return R.layout.dialog_mission_set_jump;
	}
	
	protected View buildView() {
		super.buildView();
		
		app = (DroidPlannerApp)((Activity) context).getApplication();

		jumpToList = new ArrayList<String>();

		jumpToAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_dropdown_item, jumpToList);

		findViewItems();
		setupViewItems();
		setupViewListeners();
		

		return view;
	}

	private void findViewItems() {
		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		repeatSeekBar =  (SeekBarWithText) view.findViewById(R.id.waypointRepeat);
		jumpToSpinner =  (Spinner) view.findViewById(R.id.spinnerJumpTo);
		
	}

	private void setupViewItems() {
		altitudeSeekBar.setValue(wp.getHeight());
		jumpToSpinner.setAdapter(jumpToAdapter);
		setupWPAdapter();
	}

	private void setupWPAdapter() {
		if(app != null){
			wplist = app.drone.mission.getWaypoints();
			for (int i = 0; i < wplist.size(); i++) {
				jumpToList.add("WP " + Integer.toString(i + 1) + " - " + wplist.get(i).getCmd().getName());
			}			
		}
		jumpToSpinner.setSelection((int) wp.missionItem.param1);
	}

	private void setupViewListeners() {
		altitudeSeekBar.setOnChangedListner(this);
		repeatSeekBar.setOnChangedListner(this);
		jumpToSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		wp.missionItem.param1 = (int)jumpToSpinner.getSelectedItemPosition();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}


}
