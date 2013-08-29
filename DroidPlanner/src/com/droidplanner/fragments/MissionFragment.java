package com.droidplanner.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.droidplanner.R;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.tableRow.MissionRow;

public class MissionFragment extends ListFragment{
	public ListView list;
	private Mission mission;
	private MissionRow adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.mission_fragment, container,
				false);
		list = (ListView) view.findViewById(R.id.listView1);
		adapter = new MissionRow(this.getActivity(), android.R.layout.simple_list_item_1);		
		list.setAdapter(adapter);		
		return view;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
		adapter = new MissionRow(this.getActivity(), android.R.layout.simple_list_item_1,mission.getWaypoints());
		adapter.setFragment(this);
		setListAdapter(adapter);
	}

	public void update() {
		adapter.notifyDataSetChanged();
	}

	public void onDeleteWaypoint(waypoint wp) {
		mission.removeWaypoint(wp);
	}

	public void onWaypointUpdate(waypoint wp) {
		mission.notifyMissionUpdate();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("Y", "clicked on:"+position+" view"+v.toString());
		super.onListItemClick(l, v, position, id);
	}
}
