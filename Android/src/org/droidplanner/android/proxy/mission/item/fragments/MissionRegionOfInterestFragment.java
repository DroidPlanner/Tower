package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.AbstractWheel;
import org.droidplanner.android.widgets.spinnerWheel.OnWheelChangedListener;
import org.droidplanner.android.widgets.spinnerWheel.WheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;

import android.os.Bundle;
import android.view.View;

public class MissionRegionOfInterestFragment extends MissionDetailFragment implements OnWheelChangedListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_roi;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.ROI));

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity().getApplicationContext(),
                MIN_ALTITUDE, MAX_ALTITUDE, "%d m");
        altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
        final WheelHorizontalView altitudePicker = (WheelHorizontalView) view.findViewById(R.id
                .altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.setCurrentItem(altitudeAdapter.getItemIndex((int)
                ((RegionOfInterest) itemRender.getMissionItem())
                        .getCoordinate()
                        .getAltitude()
                        .valueInMeters()));
        altitudePicker.addChangingListener(this);
	}

    @Override
    public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
        switch(wheel.getId()){
            case R.id.altitudePicker:
                ((RegionOfInterest) itemRender.getMissionItem()).setAltitude(new Altitude
                        (newValue));
                break;
        }
    }
}
