package com.droidplanner.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.widgets.adapterViews.MissionItemView;
import com.mobeta.android.dslv.HorizontalListView;

public class MissionFragment extends Fragment implements  OnWaypointChangedListner, OnItemClickListener{
	public HorizontalListView list;
	private Mission mission;
	private MissionItemView adapter;
	private OnMapInteractionListener mListner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mission, container,
				false);
		list = (HorizontalListView) view.findViewById(R.id.listView1);
		//list.setDropListener(this);
		//list.setRemoveListener(this);
		//list.setDragScrollProfile(this);
		
		adapter = new MissionItemView(this.getActivity(), android.R.layout.simple_list_item_1);		
		list.setAdapter(adapter);
		

		mission = ((DroidPlannerApp) getActivity().getApplication()).drone.mission;
		mission.addOnMissionUpdateListner(this);
		adapter = new MissionItemView(this.getActivity(), android.R.layout.simple_list_item_1,mission.getItems());
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListner = (OnMapInteractionListener) activity;
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mListner.onMarkerClick(((MissionItem) parent.getItemAtPosition(position)));		
	}

}
