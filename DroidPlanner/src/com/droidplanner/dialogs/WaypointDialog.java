package com.droidplanner.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public class WaypointDialog implements DialogInterface.OnClickListener {
	private OnWaypointUpdateListner listner;
	private waypoint wp;
	private SeekBarWithText altitudeSeekBar;
	private SpinnerSelfSelect typeSpinner;

	public WaypointDialog(waypoint wp) {
		this.wp = wp;
	}

	public void build(Context context, OnWaypointUpdateListner listner) {
		this.listner = listner;
		AlertDialog dialog = buildDialog(context);
		dialog.show();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Waypoint " + wp.getNumber());
		builder.setView(buildView(context));
		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	private View buildView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_waypoint, null);

		typeSpinner = (SpinnerSelfSelect) view
				.findViewById(R.id.spinnerWaypointType);
		ApmCommandsAdapter adapter = new ApmCommandsAdapter(context,
				android.R.layout.simple_list_item_1, ApmCommands.values());
		typeSpinner.setAdapter(adapter);

		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAltitude);
		altitudeSeekBar.setValue(wp.getHeight());

		return view;

	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			listner.onWaypointsUpdate();
		}
	}

	class ApmCommandsAdapter extends ArrayAdapter<ApmCommands> {

		public ApmCommandsAdapter(Context context, int resource,
				ApmCommands[] objects) {
			super(context, resource, objects);
		}

	}

}
