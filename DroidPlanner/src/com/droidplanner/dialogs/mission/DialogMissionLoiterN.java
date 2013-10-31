package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.LoiterTurns;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionLoiterN extends DialogMission implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {
	

	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText loiterTurnSeekBar;
	private SeekBarWithText loiterRadiusSeekBar;
	private CheckBox loiterCCW;
	private SeekBarWithText yawSeekBar;
	private LoiterTurns item;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_loitern;
	}

	protected View buildView() {
		super.buildView();
		item =  (LoiterTurns) wp;
		
		loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);
		if (item.getRadius() < 0) {
			loiterCCW.setChecked(true);
		} else {
			loiterCCW.setChecked(false);
		}
		loiterCCW.setOnCheckedChangeListener(this);

		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);
		
		loiterTurnSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTurn);
		loiterTurnSeekBar.setOnChangedListner(this);
		loiterTurnSeekBar.setValue(item.getTurns());

		loiterRadiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterRadius);
		loiterRadiusSeekBar.setAbsValue(item.getRadius());
		loiterRadiusSeekBar .setOnChangedListner(this);

		yawSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.getAngle());
		yawSeekBar.setOnChangedListner(this);

		return view;
	}


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		item.setRadius(loiterRadiusSeekBar.getValue());
		if (loiterCCW.isChecked()) {
			item.setRadius(item.getRadius()*-1.0);
		}
    }
	
	
	@Override
	public void onSeekBarChanged() {
		item.getAltitude().set(altitudeSeekBar.getValue());
		item.setTurns((int)loiterTurnSeekBar.getValue());
		item.setRadius(loiterRadiusSeekBar.getValue());
		if (loiterCCW.isChecked()) {
			item.setRadius(item.getRadius()*-1.0);
		}
		item.setAngle(yawSeekBar.getValue());
	}

}
