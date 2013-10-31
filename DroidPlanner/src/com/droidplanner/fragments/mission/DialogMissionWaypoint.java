package com.droidplanner.dialogs.mission;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.waypoints.Waypoint;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionWaypoint extends DialogMission implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {
	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText delaySeekBar;
	private SeekBarWithText yawSeekBar;
	private SeekBarWithText radiusSeekBar;
	private SeekBarWithText orbitSeekBar;
	private CheckBox orbitCCW;
	private Waypoint item;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_waypoint;
	}

	protected View buildView() {
		super.buildView();
		item = (Waypoint) wp;
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);

		delaySeekBar = (SeekBarWithText) view.findViewById(R.id.waypointDelay);
		delaySeekBar.setValue(item.delay);
		delaySeekBar.setOnChangedListner(this);

		radiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAcceptanceRadius);
		radiusSeekBar.setValue(item.acceptanceRadius);
		radiusSeekBar.setOnChangedListner(this);


		yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.yawAngle);
		yawSeekBar.setOnChangedListner(this);

		orbitSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointOrbitalRadius);
		orbitSeekBar.setOnChangedListner(this);
		orbitSeekBar.setAbsValue(item.orbitalRadius);
		
		orbitCCW = (CheckBox) view
				.findViewById(R.id.waypoint_CCW);
		orbitCCW.setChecked(item.orbitCCW);
		orbitCCW.setOnCheckedChangeListener(this);	

		return view;
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		onSeekBarChanged();
	}
	
	@Override
	public void onSeekBarChanged() {
		item.setAltitude(new Altitude(altitudeSeekBar.getValue()));
		item.delay = (float) delaySeekBar.getValue();
		item.acceptanceRadius = (float) radiusSeekBar.getValue();
		item.yawAngle = (float) yawSeekBar.getValue();
		item.orbitalRadius = (float) orbitSeekBar.getValue();
		item.orbitCCW = orbitCCW.isChecked();		
	}

}
