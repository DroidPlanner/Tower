package com.droidplanner.fragments.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.Loiter;
import com.droidplanner.drone.variables.mission.waypoints.LoiterTurns;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class MissionLoiterNFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {
	

	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText loiterTurnSeekBar;
	private SeekBarWithText loiterRadiusSeekBar;
	private CheckBox loiterCCW;
	private SeekBarWithText yawSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_detail_loitern;
	}
	
	@Override
	protected void setupViews(View view) {	
		super.setupViews(view);	
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.LOITERN));
		
		LoiterTurns item = (LoiterTurns) this.item;
		loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);
		loiterCCW.setChecked(item.isOrbitCCW());
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
		loiterRadiusSeekBar.setAbsValue(item.getOrbitalRadius());
		loiterRadiusSeekBar .setOnChangedListner(this);

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
		LoiterTurns item = (LoiterTurns) this.item;
		item.getAltitude().set(altitudeSeekBar.getValue());
		item.setTurns((int)loiterTurnSeekBar.getValue());
		item.setOrbitalRadius(loiterRadiusSeekBar.getValue());
		item.setYawAngle(yawSeekBar.getValue());
	}

}
