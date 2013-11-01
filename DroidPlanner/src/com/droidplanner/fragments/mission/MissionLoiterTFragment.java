package com.droidplanner.fragments.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.LoiterTime;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionLoiterTFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText loiterTimeSeekBar;
	private SeekBarWithText loiterRadiusSeekBar;
	private CheckBox loiterCCW;
	private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_detail_loitert;
	}
	

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);		
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.LOITERT));

		LoiterTime item = (LoiterTime) this.item;
		
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
		
		loiterTimeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterTime);
		loiterTimeSeekBar .setOnChangedListner(this);
		loiterTimeSeekBar.setValue(item.getTime());
		
		loiterRadiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.loiterRadius);
		loiterRadiusSeekBar.setAbsValue(item.getRadius());
		loiterRadiusSeekBar .setOnChangedListner(this);

		yawSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.getAngle());
		yawSeekBar.setOnChangedListner(this);
	}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	LoiterTime item = (LoiterTime) this.item;
    	item.setRadius(loiterRadiusSeekBar.getValue());
		if (loiterCCW.isChecked()) {
			item.setRadius(item.getRadius()*-1.0);
		}
    }
	

	@Override
	public void onSeekBarChanged() {
		LoiterTime item = (LoiterTime) this.item;
		item.getAltitude().set(altitudeSeekBar.getValue());
		item.setTime(loiterTimeSeekBar.getValue());
		item.setRadius(loiterRadiusSeekBar.getValue());
		if (loiterCCW.isChecked()) {
			item.setRadius(item.getRadius()*-1.0);
		}
		item.setAngle(yawSeekBar.getValue());
	}


}
