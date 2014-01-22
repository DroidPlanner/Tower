package org.droidplanner.fragments.mission;

import org.droidplanner.drone.variables.mission.waypoints.Loiter;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListener;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.droidplanner.R;

public class MissionLoiterFragment extends MissionDetailFragment implements
		OnTextSeekBarChangedListener, OnCheckedChangeListener{


	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_loiter;
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
