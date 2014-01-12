package org.droidplanner.fragments.mission;

import org.droidplanner.drone.variables.mission.waypoints.Loiter;
import org.droidplanner.drone.variables.mission.waypoints.LoiterTime;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.droidplanner.R;

public class MissionLoiterTFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener {

	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText loiterTimeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_loitert;
	}


	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.LOITERT));

		LoiterTime item = (LoiterTime) this.item;

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListner(this);

		loiterTimeSeekBar = (SeekBarWithText) view.findViewById(R.id.loiterTime);
		loiterTimeSeekBar .setOnChangedListner(this);
		loiterTimeSeekBar.setValue(item.getTime());

	}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((Loiter) item).setOrbitCCW(isChecked);
    }


	@Override
	public void onSeekBarChanged() {
		LoiterTime item = (LoiterTime) this.item;

		item.getAltitude().set(altitudeSeekBar.getValue());
		item.setTime(loiterTimeSeekBar.getValue());
		//item.setOrbitalRadius(loiterRadiusSeekBar.getValue());
		//item.setYawAngle(yawSeekBar.getValue());
	}


}
