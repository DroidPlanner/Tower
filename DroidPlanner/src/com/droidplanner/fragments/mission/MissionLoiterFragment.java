package com.droidplanner.fragments.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.Loiter;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionLoiterFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener{

	private SeekBarWithText loiterRadiusSeekBar;
	private CheckBox loiterCCW;
	private SeekBarWithText yawSeekBar;
	
	@Override
	protected int getResource() {
		return R.layout.fragment_detail_loiter;
	}
	
	
	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.LOITER));
		
		loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);
		if (((Loiter) item).getRadius()< 0) {
			loiterCCW.setChecked(true);
		} else {
			loiterCCW.setChecked(false);
		}
		loiterCCW.setOnCheckedChangeListener(this);


		loiterRadiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterRadius);
		loiterRadiusSeekBar .setOnChangedListner(this);
		loiterRadiusSeekBar.setAbsValue(((Loiter) item).getRadius());

		yawSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(((Loiter) item).getAngle());
		yawSeekBar.setOnChangedListner(this);
	}
	
	

	@Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((Loiter) item).setRadius(loiterRadiusSeekBar.getValue());
		if (loiterCCW.isChecked()) {
			((Loiter) item).setRadius(((Loiter) item).getRadius()*-1.0);
		}
    }
	
	@Override
	public void onSeekBarChanged() {
		((Loiter) item).setRadius(loiterRadiusSeekBar.getValue());
		if (loiterCCW.isChecked()) {
			((Loiter) item).setRadius(((Loiter) item).getRadius()*-1.0);
		}
		((Loiter) item).setAngle(yawSeekBar.getValue());
	}
}
