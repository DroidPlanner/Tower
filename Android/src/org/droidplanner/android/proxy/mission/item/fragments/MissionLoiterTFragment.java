package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.Loiter;
import org.droidplanner.core.mission.waypoints.LoiterTime;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MissionLoiterTFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener, OnCheckedChangeListener {

	private SeekBarWithText altitudeSeekBar;
	private SeekBarWithText loiterTimeSeekBar;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_loitert;
	}


	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.LOITERT));

		LoiterTime item = (LoiterTime) this.itemRender.getMissionItem();

		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
		altitudeSeekBar.setValue(item.getCoordinate().getAltitude().valueInMeters());
		altitudeSeekBar.setOnChangedListener(this);

		loiterTimeSeekBar = (SeekBarWithText) view.findViewById(R.id.loiterTime);
		loiterTimeSeekBar .setOnChangedListener(this);
		loiterTimeSeekBar.setValue(item.getTime());

	}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((Loiter) itemRender.getMissionItem()).setOrbitCCW(isChecked);
    }


	@Override
	public void onSeekBarChanged() {
		LoiterTime item = (LoiterTime) this.itemRender.getMissionItem();

		item.getCoordinate().getAltitude().set(altitudeSeekBar.getValue());
		item.setTime(loiterTimeSeekBar.getValue());
		//item.setOrbitalRadius(loiterRadiusSeekBar.getValue());
		//item.setYawAngle(yawSeekBar.getValue());
	}


}
