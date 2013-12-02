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


	@Override
	protected int getResource() {
		return R.layout.fragment_detail_loiter;
	}


	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		//typeSpinner.setSelection(commandAdapter.getPosition(MissionItemTypes.LOITER));

		//LoiterInfinite item = (LoiterInfinite) this.item;
	}



	@Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((Loiter) item).setOrbitCCW(isChecked);
    }

	@Override
	public void onSeekBarChanged() {
		//Takeoff item = (Takeoff) this.item;
		//((Loiter) item).setOrbitalRadius(loiterRadiusSeekBar.getValue());
	}
}
