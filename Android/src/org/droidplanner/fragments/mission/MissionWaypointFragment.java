package org.droidplanner.fragments.mission;

import org.droidplanner.R;
import org.droidplanner.mission.*;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListener;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MissionWaypointFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListener, OnCheckedChangeListener {

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

		WaypointD item = (WaypointD) this.item;

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListener(this);

		delaySeekBar = (SeekBarWithText) view.findViewById(R.id.waypointDelay);
		delaySeekBar.setValue(item.getDelay());
		delaySeekBar.setOnChangedListener(this);

		/*
		radiusSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointAcceptanceRadius);
		radiusSeekBar.setValue(item.getAcceptanceRadius());
		radiusSeekBar.setOnChangedListener(this);

		yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
		yawSeekBar.setValue(item.getYawAngle());
		yawSeekBar.setOnChangedListener(this);

		orbitSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointOrbitalRadius);
		orbitSeekBar.setOnChangedListener(this);
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
		WaypointD item = (WaypointD) this.item;
		item.setAltitude(new Altitude(altitudeSeekBar.getValue()));
		item.setDelay((float) delaySeekBar.getValue());
		//item.setAcceptanceRadius((float) radiusSeekBar.getValue());
		//item.setYawAngle((float) yawSeekBar.getValue());
		//item.setOrbitalRadius((float) orbitSeekBar.getValue());
		//item.setOrbitCCW(orbitCCW.isChecked());
	}

}
