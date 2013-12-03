package com.droidplanner.fragments;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemLongClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemSelectedListener;
import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.OnEditorInteraction;
import com.droidplanner.drone.DroneInterfaces.OnWaypointChangedListner;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.widgets.adapterViews.MissionItemView;

public class MissionFragment extends Fragment implements  OnWaypointChangedListner, OnItemLongClickListener,  OnItemClickListener, OnItemSelectedListener{
	public HListView list;
	private Mission mission;
	private MissionItemView adapter;
	private OnEditorInteraction editorListner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mission, container,
				false);
		list = (HListView) view.findViewById(R.id.listView1);
		
		mission = ((DroidPlannerApp) getActivity().getApplication()).drone.mission;
		mission.addOnMissionUpdateListner(this);
		adapter = new MissionItemView(this.getActivity(), android.R.layout.simple_list_item_1,mission.getItems());
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		list.setAdapter(adapter);

		Log.i( "LIST", "choice mode: " + list.getChoiceMode() );

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		editorListner = (OnEditorInteraction) ( activity);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mission.removeOnMissionUpdateListner(this);
	}

	public void update() {
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
	public void onMissionUpdate() {
		update();		
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
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub


		Log.d("LIST", "you onItemSelected "+ position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub


		Log.d("LIST", "you onNothingSelected ");
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Log.d("LIST", "you onItemClick "+ position);		
		MissionItem missionItem = (MissionItem) adapter.getItemAtPosition(position);
		editorListner.onItemClick(missionItem);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view,
			int position, long id) {
		Log.d("LIST", "you longcliked item "+position);
		MissionItem missionItem = (MissionItem) adapter.getItemAtPosition(position);
		return editorListner.onItemLongClick(missionItem);
	}

}
