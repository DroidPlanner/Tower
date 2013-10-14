package com.droidplanner.dialogs.mission;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionExtra extends DialogMission implements OnTextSeekBarChangedListner{
	SeekBarWithText altitudeSeekBar;
	SeekBarWithText delaySeekBar;
	SeekBarWithText loiterTurnSeekBar;
	SeekBarWithText loiterTimeSeekBar;
	FrameLayout optionLayout;
	CheckBox loiterCCW;
	public DialogMissionExtra(waypoint wp) {
		this.wp = wp;
	}

	protected View buildView(Context context) {		

		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
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
				.findViewById(R.id.waypointOrbitalRadius);
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
	protected int getResource() {
		// TODO Auto-generated method stub
		return 0;
	}

}
