package com.droidplanner.dialogs.mission;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.dialogs.listener.helpers.setJumpItemSelectedListener;
import com.droidplanner.drone.variables.mission.waypoints.GenericWaypoint;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionSetJump extends DialogMission implements
		OnTextSeekBarChangedListner {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText repeatSeekBar;
	private Spinner jumpToSpinner;
	private ArrayAdapter<String> jumpToAdapter;
	private ArrayList<String> jumpToList;
	private List<GenericWaypoint> wplist;
	private DroidPlannerApp app;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_set_jump;
	}

	protected View buildView() {
		super.buildView();
		findViewItems();
		setupViewItems();
		setupViewListeners();
		return view;
	}

	private void findViewItems() {
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		repeatSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointRepeat);
		jumpToSpinner = (Spinner) view.findViewById(R.id.spinnerJumpTo);
	}

	private void setupViewItems() {
		altitudeSeekBar.setValue(wp.getHeight());
		repeatSeekBar.setValue((int) wp.missionItem.param2);
		setupWPList();
	}

	private void setupViewListeners() {
		altitudeSeekBar.setOnChangedListner(this);
		repeatSeekBar.setOnChangedListner(this);
		jumpToSpinner
				.setOnItemSelectedListener(new setJumpItemSelectedListener(this));
	}

	private void setupWPList() {
		app = (DroidPlannerApp) ((Activity) context).getApplication();

		jumpToList = new ArrayList<String>();

		if (app != null) {
			wplist = app.drone.mission.getWaypoints();
			for (int i = 0; i < wplist.size(); i++) {
				jumpToList.add("WP " + Integer.toString(i + 1) + " - "
						+ wplist.get(i).getCmd().getName());
			}
		}

		jumpToAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item, jumpToList);
		jumpToSpinner.setAdapter(jumpToAdapter);

		if (jumpToSpinner.getSelectedItemPosition() == -1)
			jumpToSpinner.setSelection(wp.getNumber());
		else
			jumpToSpinner.setSelection((int) wp.missionItem.param1);
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param2 = (float) repeatSeekBar.getValue();
	}

	public void setJump(int arg2) {
		// TODO Auto-generated method stub
		wp.missionItem.param1 = arg2;

	}

}
