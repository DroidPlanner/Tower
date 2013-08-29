package com.droidplanner.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
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
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public class WaypointDialog implements DialogInterface.OnClickListener, OnItemSelectedListener, OnTextSeekBarChangedListner {
	private OnWaypointUpdateListner listner;
	private waypoint wp;
	private SeekBarWithText altitudeSeekBar;
	private SpinnerSelfSelect typeSpinner;
	private ApmCommandsAdapter commandAdapter;

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
		builder.setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	private View buildView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_waypoint, null);

		typeSpinner = (SpinnerSelfSelect) view
				.findViewById(R.id.spinnerWaypointType);
		commandAdapter = new ApmCommandsAdapter(context,
				android.R.layout.simple_list_item_1, ApmCommands.values());
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);
		typeSpinner.setSelection(commandAdapter.getPosition(wp.getCmd()));
		

		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAltitude);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);
		

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
		Log.d("Q", "selected"+commandAdapter.getItem(position).getName());
		wp.setCmd(commandAdapter.getItem(position));
	}
	
	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
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
			((TextView)view).setText(getItem(position).getName());
			return view;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			return getView(position, convertView, parent);
		}

	}



}
