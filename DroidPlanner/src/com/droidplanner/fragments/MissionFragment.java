package com.droidplanner.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.dialogs.WaypointDialog;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.tableRow.MissionRow;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DragScrollProfile;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class MissionFragment extends ListFragment implements DragScrollProfile, RemoveListener, DropListener, OnWaypointUpdateListner{
	public DragSortListView list;
	private Mission mission;
	private MissionRow adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.mission_fragment, container,
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
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void drop(int from, int to) {
		waypoint item=adapter.getItem(from);
        adapter.remove(item);
        adapter.insert(item, to);
        adapter.notifyDataSetChanged();
        mission.notifyMissionUpdate();		
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

	public void onWaypointUpdate(waypoint waypoint) {
		mission.notifyMissionUpdate();		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("T", "touched "+position);
		WaypointDialog dialog = new WaypointDialog(adapter.getItem(position));
		dialog.build(this.getActivity(), this);		
		super.onListItemClick(l, v, position, id);
	}
	
	@Override
	public void onWaypointsUpdate() {
		Log.d("T", "waypoint updated by dialog");
		mission.notifyMissionUpdate();	
		adapter.notifyDataSetChanged();
	}
}
