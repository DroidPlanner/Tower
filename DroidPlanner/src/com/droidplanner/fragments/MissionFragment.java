package com.droidplanner.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.R;
import com.droidplanner.dialogs.mission.DialogMissionFactory;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.tableRow.MissionRow;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DragScrollProfile;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class MissionFragment extends ListFragment implements DragScrollProfile, RemoveListener, DropListener, OnWaypointChangedListner{
	public DragSortListView list;
	private Mission mission;
	private MissionRow adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mission, container,
				false);
		list = (DragSortListView) view.findViewById(R.id.listView1);
		list.setDropListener(this);
		list.setRemoveListener(this);
		list.setDragScrollProfile(this);
		
		adapter = new MissionRow(this.getActivity(), android.R.layout.simple_list_item_1);		
		list.setAdapter(adapter);		
		return view;
	}

	public void setMission(Mission mission) {
		this.mission = mission;
		adapter = new MissionRow(this.getActivity(), android.R.layout.simple_list_item_1,mission.getWaypoints());
		setListAdapter(adapter);
	}

	public void update() {
		waypoint.updateDistancesFromPrevPoint(mission.getWaypoints());
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void drop(int from, int to) {
		waypoint item=adapter.getItem(from);
        adapter.remove(item);
        adapter.insert(item, to);
        mission.reNumberWaypoints();
        adapter.notifyDataSetChanged();
        mission.onWaypointsUpdate();		
	}

	@Override
	public void remove(int which) {
		mission.removeWaypoint(adapter.getItem(which));
	}

	@Override
	public float getSpeed(float w, long t) {
        if (w > 0.8f) {
            // Traverse all views in a millisecond
            return ((float) adapter.getCount()) / 0.001f;
        } else {
            return 5.0f * w;
        }
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("T", "touched "+position);
		DialogMissionFactory.getDialog(adapter.getItem(position), this.getActivity(), mission);		
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onWaypointsUpdate() {
		update();		
	}

}
