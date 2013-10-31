package com.droidplanner.fragments.mission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public abstract class DialogMission implements OnItemSelectedListener,
		OnClickListener {
	protected abstract int getResource();

	private OnWaypointChangedListner listner;
	private SpinnerSelfSelect typeSpinner;
	private ApmCommandsAdapter commandAdapter;
	private AlertDialog dialog;
	protected Context context;
	protected MissionItem wp;
	protected View view;

	public void build(MissionItem wp, Context context,
			OnWaypointChangedListner listner) {
		this.wp = wp;
		this.listner = listner;
		this.context = context;
		dialog = buildDialog();
		dialog.show();
	}

	private AlertDialog buildDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		//builder.settitle("waypoint " + wp.getnumber());
		builder.setTitle("waypoint");
		builder.setView(buildView());
		builder.setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	protected View buildView() {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(getResource(), null);

		typeSpinner = (SpinnerSelfSelect) view
				.findViewById(R.id.spinnerWaypointType);
		commandAdapter = new ApmCommandsAdapter(context,
				android.R.layout.simple_list_item_1, ApmCommands.values());
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);
		//typeSpinner.setSelection(commandAdapter.getPosition(wp.getCmd()));
		typeSpinner.setSelection(commandAdapter.getPosition(ApmCommands.CMD_NAV_WAYPOINT));
		return view;

	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			listner.onMissionUpdate();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position,
			long id) {
		/*
		ApmCommands selected = commandAdapter.getItem(position);
		if (selected != wp.getCmd()) {
			wp.setCmd(selected);
			dialog.dismiss();
			DialogMissionFactory.getDialog(wp, context, listner);
		}
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