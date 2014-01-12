package org.droidplanner.fragments.mission;

import org.droidplanner.drone.variables.mission.waypoints.Waypoint;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.droidplanner.R;

public class MissionWaypointFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {

	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText delaySeekBar;

	//private SeekBarWithText yawSeekBar;
	//private SeekBarWithText radiusSeekBar;
	//private SeekBarWithText orbitSeekBar;
	//private CheckBox orbitCCW;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_waypoint;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.WAYPOINT));

		Waypoint item = (Waypoint) this.item;

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);

		delaySeekBar = (SeekBarWithText) view.findViewById(R.id.waypointDelay);
		delaySeekBar.setValue(item.getDelay());
		delaySeekBar.setOnChangedListner(this);

		/*
		radiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAcceptanceRadius);
		radiusSeekBar.setValue(item.getAcceptanceRadius());
		radiusSeekBar.setOnChangedListner(this);

		yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.getYawAngle());
		yawSeekBar.setOnChangedListner(this);

		orbitSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointOrbitalRadius);
		orbitSeekBar.setOnChangedListner(this);
		orbitSeekBar.setAbsValue(item.getOrbitalRadius());

		orbitCCW = (CheckBox) view.findViewById(R.id.waypoint_CCW);
		orbitCCW.setChecked(item.isOrbitCCW());
		orbitCCW.setOnCheckedChangeListener(this);
		*/
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		onSeekBarChanged();
	}

	@Override
	public void onSeekBarChanged() {
		Waypoint item = (Waypoint) this.item;
		item.setAltitude(new Altitude(altitudeSeekBar.getValue()));
		item.setDelay((float) delaySeekBar.getValue());
		//item.setAcceptanceRadius((float) radiusSeekBar.getValue());
		//item.setYawAngle((float) yawSeekBar.getValue());
		//item.setOrbitalRadius((float) orbitSeekBar.getValue());
		//item.setOrbitCCW(orbitCCW.isChecked());
	}

}
