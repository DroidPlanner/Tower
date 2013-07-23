package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.droidplanner.R;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.tableRow.MissionRow;

public class MissionFragment extends Fragment {
	public TableLayout table;
	private Mission mission;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.mission_fragment, container,
				false);
		table = (TableLayout) view.findViewById(R.id.missionTable);
		return view;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	public void update() {
		table.removeAllViews();
		int i = 0;
		for (waypoint wp : mission.getWaypoints()) {
			addRow(wp, i++);
		}
	}

	private void addRow(waypoint wp, int i) {
		MissionRow row = new MissionRow(this, wp);
		table.addView(row);
	}

	public void onDeleteWaypoint(waypoint waypoint) {
		mission.removeWaypoint(waypoint);
	}
}
