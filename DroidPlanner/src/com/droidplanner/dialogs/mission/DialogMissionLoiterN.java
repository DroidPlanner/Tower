package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionLoiterN extends DialogMission implements
		OnTextSeekBarChangedListner {
	

	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText loiterTurnSeekBar;
	private CheckBox loiterCCW;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_loitern;
	}

	protected View buildView() {
		super.buildView();

		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAltitude);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);
		
		loiterTurnSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTurn);
		loiterTurnSeekBar.setOnChangedListner(this);

		loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);
		loiterCCW.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	        @Override
	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	    		if (loiterCCW.isChecked()) {
	    			wp.missionItem.param3 *= -1.0;
	    		}
	        }
	    });

		if (wp.missionItem.param1 < 0) {
			loiterCCW.setChecked(true);
			loiterTurnSeekBar.setValue(-1.0 * wp.missionItem.param1);
		} else {
			loiterCCW.setChecked(false);
			loiterTurnSeekBar.setValue(wp.missionItem.param1);
		}

		return view;
	}



	
	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
		wp.missionItem.param1 = (float) loiterTurnSeekBar.getValue();
		if (loiterCCW.isChecked()) {
			wp.missionItem.param3 *= -1.0;
		}
	}

}
