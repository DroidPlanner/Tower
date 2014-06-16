package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.waypoints.Waypoint;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListener;


import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MissionWaypointFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListener, OnCheckedChangeListener {

	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText delaySeekBar;

	// private SeekBarWithText yawSeekBar;
	// private SeekBarWithText radiusSeekBar;
	// private SeekBarWithText orbitSeekBar;
	// private CheckBox orbitCCW;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_waypoint;
	}

	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemType.WAYPOINT));

		Waypoint item = (Waypoint) this.itemRender.getMissionItem();

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getCoordinate().getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListener(this);

		delaySeekBar = (SeekBarWithText) view.findViewById(R.id.waypointDelay);
		delaySeekBar.setValue(item.getDelay());
		delaySeekBar.setOnChangedListener(this);

		/*
		 * radiusSeekBar = (SeekBarWithText) view
		 * .findViewById(R.id.waypointAcceptanceRadius);
		 * radiusSeekBar.setValue(item.getAcceptanceRadius());
		 * radiusSeekBar.setOnChangedListener(this);
		 * 
		 * yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		 * yawSeekBar.setValue(item.getYawAngle());
		 * yawSeekBar.setOnChangedListener(this);
		 * 
		 * orbitSeekBar = (SeekBarWithText) view
		 * .findViewById(R.id.waypointOrbitalRadius);
		 * orbitSeekBar.setOnChangedListener(this);
		 * orbitSeekBar.setAbsValue(item.getOrbitalRadius());
		 * 
		 * orbitCCW = (CheckBox) view.findViewById(R.id.waypoint_CCW);
		 * orbitCCW.setChecked(item.isOrbitCCW());
		 * orbitCCW.setOnCheckedChangeListener(this);
		 */
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		onSeekBarChanged();
	}

	@Override
	public void onSeekBarChanged() {
		Waypoint item = (Waypoint) this.itemRender.getMissionItem();
		item.setAltitude(new Altitude(altitudeSeekBar.getValue()));
		item.setDelay((float) delaySeekBar.getValue());
		// item.setAcceptanceRadius((float) radiusSeekBar.getValue());
		// item.setYawAngle((float) yawSeekBar.getValue());
		// item.setOrbitalRadius((float) orbitSeekBar.getValue());
		// item.setOrbitCCW(orbitCCW.isChecked());
		item.getMission().notifyMissionUpdate();		
	}

}
