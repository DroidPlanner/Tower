package com.droidplanner.fragments;

import it.sephiroth.android.library.util.v11.MultiChoiceModeListener;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemLongClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemSelectedListener;
import it.sephiroth.android.library.widget.HListView;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.widgets.adapterViews.MissionItemView;

public class MissionFragment extends Fragment implements  OnWaypointChangedListner, OnItemLongClickListener,  OnItemClickListener, OnItemSelectedListener, MultiChoiceModeListener{
	public HListView list;
	private Mission mission;
	private MissionItemView adapter;
	private OnMapInteractionListener mListner;

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
		list.setMultiChoiceModeListener( this );		
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		
		list.setAdapter(adapter);

		Log.i( "LIST", "choice mode: " + list.getChoiceMode() );

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
	public boolean onActionItemClicked(ActionMode arg0, MenuItem arg1) {
		// TODO Auto-generated method stub

		Log.d("LIST", "you onActionItemClicked ");
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode arg0, Menu menu) {
		// TODO Auto-generated method stub

		Log.d("LIST", "you onCreateActionMode ");
		menu.add( 0, 0, 0, "Delete" );
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		// TODO Auto-generated method stub

		Log.d("LIST", "you onDestroyActionMode ");
		
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		// TODO Auto-generated method stub

		Log.d("LIST", "you onPrepareActionMode ");
		return false;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		// TODO Auto-generated method stub

		Log.d("LIST", "you onItemCheckedStateChanged "+ position);
		
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mListner.onMarkerClick(((MissionItem) parent.getItemAtPosition(position)));
		Log.d("LIST", "you onItemClick "+ position);		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		Log.d("LIST", "you onItemLongClick "+ position);
		return false;
	}


}
