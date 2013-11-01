package com.droidplanner.fragments.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.Loiter;
import com.droidplanner.drone.variables.mission.waypoints.LoiterInfinite;
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
		
		LoiterInfinite item = (LoiterInfinite) this.item;
		
		
		loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);
		loiterCCW.setChecked(item.isOrbitCCW());
		loiterCCW.setOnCheckedChangeListener(this);


		loiterRadiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterRadius);
		loiterRadiusSeekBar .setOnChangedListner(this);
		loiterRadiusSeekBar.setAbsValue(item.getOrbitalRadius());

		yawSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.getYawAngle());
		yawSeekBar.setOnChangedListner(this);
	}
	
	

	@Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((Loiter) item).setOrbitCCW(isChecked);
    }
	
	@Override
	public void onSeekBarChanged() {
		((Loiter) item).setOrbitalRadius(loiterRadiusSeekBar.getValue());
		((Loiter) item).setYawAngle(yawSeekBar.getValue());
	}
}
