package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.adapterViews.MissionItem;
import com.mobeta.android.dslv.HorizontalListView;

public class MissionFragment extends Fragment implements  OnWaypointChangedListner{
	public HorizontalListView list;
	private Mission mission;
	private MissionItem adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mission, container,
				false);
		list = (HorizontalListView) view.findViewById(R.id.listView1);
		//list.setDropListener(this);
		//list.setRemoveListener(this);
		//list.setDragScrollProfile(this);
		
		adapter = new MissionItem(this.getActivity(), android.R.layout.simple_list_item_1);		
		list.setAdapter(adapter);
		

		mission = ((DroidPlannerApp) getActivity().getApplication()).drone.mission;
		mission.addOnWaypointsChangedListner(this);
		adapter = new MissionItem(this.getActivity(), android.R.layout.simple_list_item_1,mission.getWaypoints());
		list.setAdapter(adapter);	

		return view;
	}

	@Override
	public void onStop() {
		super.onStop();
		mission.removeOnWaypointsChangedListner(this);
	}

	public void update() {
		waypoint.updateDistancesFromPrevPoint(mission.getWaypoints());
		adapter.notifyDataSetChanged();
	}
	
	/*
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("T", "touched "+position);
		DialogMissionFactory.getDialog(adapter.getItem(position), this.getActivity(), mission);		
		super.onListItemClick(l, v, position, id);
	}*/

	@Override
	public void onWaypointsUpdate() {
		update();		
	}

}
