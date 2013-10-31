package com.droidplanner.fragments.mission;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public abstract class MissionDetailFragment extends Fragment implements
		OnItemSelectedListener {
	public abstract void setItem(MissionItem item);
	protected abstract int getResource();
	
	private SpinnerSelfSelect typeSpinner;
	private ApmCommandsAdapter commandAdapter;
	protected Mission mission;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(getResource(), null);
		setupViews(view);
		return view;
	}

	protected void setupViews(View view) {
		typeSpinner = (SpinnerSelfSelect) view
				.findViewById(R.id.spinnerWaypointType);
		commandAdapter = new ApmCommandsAdapter(this.getActivity(),
				android.R.layout.simple_list_item_1, ApmCommands.values());
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);
		// typeSpinner.setSelection(commandAdapter.getPosition(wp.getCmd()));
		typeSpinner.setSelection(commandAdapter
				.getPosition(ApmCommands.CMD_NAV_WAYPOINT));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mission = ((DroidPlannerApp) getActivity().getApplication()).drone.mission;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position,
			long id) {
		/*
		 * ApmCommands selected = commandAdapter.getItem(position); if (selected
		 * != wp.getCmd()) { wp.setCmd(selected); dialog.dismiss();
		 * DialogMissionFactory.getDialog(wp, context, listner); }
		 */
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	class ApmCommandsAdapter extends ArrayAdapter<ApmCommands> {

		public ApmCommandsAdapter(Context context, int resource,
				ApmCommands[] objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			((TextView) view).setText(getItem(position).getName());
			return view;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getView(position, convertView, parent);
		}

	}

}