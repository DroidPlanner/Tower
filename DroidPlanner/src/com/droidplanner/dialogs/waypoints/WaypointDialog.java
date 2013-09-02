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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public class WaypointDialog implements DialogInterface.OnClickListener, OnItemSelectedListener, OnTextSeekBarChangedListner, OnCheckedChangeListener{
	private OnWaypointUpdateListner listner;
	private waypoint wp;
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText delaySeekBar;
	private SeekBarWithText loiterTurnSeekBar;
	private SeekBarWithText loiterTimeSeekBar;
	private FrameLayout optionLayout;
	private CheckBox loiterCCW;
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
		altitudeSeekBar .setValue(wp.getHeight());
		altitudeSeekBar .setOnChangedListner(this);
		
		delaySeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointDelay);
		delaySeekBar .setValue((double)wp.missionItem.param2);
		delaySeekBar .setOnChangedListner(this);
		
		loiterTimeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTime);
		loiterTimeSeekBar .setOnChangedListner(this);
		
		loiterTurnSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTurn);
		loiterTurnSeekBar .setOnChangedListner(this);

		loiterCCW = (CheckBox) view
				.findViewById(R.string.loiter_ccw);

		optionLayout = (FrameLayout) view
				.findViewById(R.id.optionLayout);
		
		delaySeekBar.setVisibility(8);
		loiterTimeSeekBar .setVisibility(8);
		optionLayout .setVisibility(8);
		
		if(wp.getCmd().toString()=="CMD_NAV_LOITER_TURNS"){
			optionLayout.setVisibility(0);
			
			if(wp.missionItem.param1<0){
				loiterCCW.setChecked(true);
				loiterTurnSeekBar.setValue(-1.0*wp.missionItem.param1);
			}
			else {
				loiterCCW.setChecked(false);
				loiterTurnSeekBar.setValue(wp.missionItem.param1);
			}
		} 
		else if (wp.getCmd().toString()=="CMD_NAV_LOITER_TIME"){
			loiterTimeSeekBar.setVisibility(0);
			
			loiterTimeSeekBar.setValue(wp.missionItem.param1);
		}
		else if (wp.getCmd().toString()=="CMD_NAV_LOITER_UNLIM"){
			delaySeekBar.setVisibility(8);
		} 
		else {
			delaySeekBar.setVisibility(0);
			delaySeekBar.setValue(wp.missionItem.param1);
		}
			
			

		return view;

	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		
		if(wp.getCmd().toString()=="CMD_NAV_LOITER_TURNS" && loiterCCW.isChecked() && wp.missionItem.param1>0) {
			wp.missionItem.param1 *= -1.0; 
		}
		
		if (which == Dialog.BUTTON_POSITIVE) {
			listner.onWaypointsUpdate();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position,
			long id) {
		Log.d("Q", "selected"+commandAdapter.getItem(position).getName());

		delaySeekBar.setVisibility(0);
		loiterTimeSeekBar .setVisibility(8);
		optionLayout .setVisibility(8);

		if(commandAdapter.getItem(position).getName()=="LoiterN"){
			delaySeekBar.setVisibility(8);
			optionLayout.setVisibility(0);
		}
		else if(commandAdapter.getItem(position).getName()=="LoiterT"){
			delaySeekBar.setVisibility(8);
			loiterTimeSeekBar.setVisibility(0);
		}
		else if(commandAdapter.getItem(position).getName()=="Loiter"){
			delaySeekBar.setVisibility(8);
		}

		wp.setCmd(commandAdapter.getItem(position));
	}
	
	@Override
	public void onSeekBarChanged() {
	
		wp.setHeight(altitudeSeekBar.getValue());
		
		if(wp.getCmd().toString()=="CMD_NAV_LOITER_TURNS"){
			wp.missionItem.param1 = (float) loiterTurnSeekBar.getValue();
			if(loiterCCW.isChecked()) {
				wp.missionItem.param3 *= -1.0; 
			}
		}
		else if(wp.getCmd().toString()=="CMD_NAV_LOITER_TIME"){
			wp.missionItem.param1 = (float) loiterTimeSeekBar.getValue();
		}
		else 
		{
			wp.missionItem.param1 = (float) delaySeekBar.getValue();
		}
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
	}



}
