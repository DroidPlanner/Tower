package com.droidplanner.dialogs.waypoints;

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
import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public abstract class WaypointDialog implements OnItemSelectedListener,
		OnClickListener {
	protected abstract int getResource();

	private OnWaypointUpdateListner listner;
	private SpinnerSelfSelect typeSpinner;
	private ApmCommandsAdapter commandAdapter;
	protected waypoint wp;
	protected View view;

	public void build(waypoint wp, Context context,
			OnWaypointUpdateListner listner) {
		this.wp = wp;
		this.listner = listner;
		AlertDialog dialog = buildDialog(context);
		dialog.show();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Waypoint " + wp.getNumber());
		builder.setView(buildView(context));
		builder.setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	protected View buildView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(getResource(), null);

		typeSpinner = (SpinnerSelfSelect) view
				.findViewById(R.id.spinnerWaypointType);
		commandAdapter = new ApmCommandsAdapter(context,
				android.R.layout.simple_list_item_1, ApmCommands.values());
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);
		typeSpinner.setSelection(commandAdapter.getPosition(wp.getCmd()));

		return view;

	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			listner.onWaypointsUpdate();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position,
			long id) {
		wp.setCmd(commandAdapter.getItem(position));
		view.invalidate();
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