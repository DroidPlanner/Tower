package com.droidplanner.fragments;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemLongClickListener;
import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.OnEditorInteraction;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.widgets.adapterViews.MissionItemView;

public class EditorListFragment extends Fragment implements  OnItemLongClickListener,  OnItemClickListener, OnDroneListner{
	public HListView list;
	private Mission mission;
	private MissionItemView adapter;
	private OnEditorInteraction editorListner;
	private ImageButton leftArrow;
	private ImageButton rightArrow;
	private Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_editor_list, container,
				false);
		list = (HListView) view.findViewById(R.id.listView1);
		leftArrow = (ImageButton) view.findViewById(R.id.listLeftArrow);
		rightArrow = (ImageButton) view.findViewById(R.id.listRightArrow);
		
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		mission = drone.mission;
		adapter = new MissionItemView(this.getActivity(), android.R.layout.simple_list_item_1,mission.getItems());
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		list.setAdapter(adapter);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		editorListner = (OnEditorInteraction) ( activity);
	}

	@Override
	public void onDroneEvent(DroneEventsType event) {
		if (event == DroneEventsType.MISSION) {
			adapter.notifyDataSetChanged();			
		}		
	}

	public void deleteSelected() {
		SparseBooleanArray selected = list.getCheckedItemPositions();
		ArrayList<MissionItem> toRemove = new ArrayList<MissionItem>();
		
		for( int i = 0; i < selected.size(); i++ ) {
			if( selected.valueAt( i ) ) {
				MissionItem item = adapter.getItem(selected.keyAt(i));
				toRemove.add(item);
			}
		}		
		
		mission.removeWaypoints(toRemove);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {	
		MissionItem missionItem = (MissionItem) adapter.getItemAtPosition(position);
		editorListner.onItemClick(missionItem);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view,
			int position, long id) {
		MissionItem missionItem = (MissionItem) adapter.getItemAtPosition(position);
		return editorListner.onItemLongClick(missionItem);
	}

	public void setArrowsVisibility(boolean visible) {
		if (visible) {
			leftArrow.setVisibility(View.VISIBLE);
			rightArrow.setVisibility(View.VISIBLE);			
		}else{
			leftArrow.setVisibility(View.INVISIBLE);
			rightArrow.setVisibility(View.INVISIBLE);
		}
	}

}
