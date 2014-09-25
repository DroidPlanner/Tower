package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.LoiterTime;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class MissionLoiterTFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelChangedListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_loitert;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        final Context context = getActivity().getApplicationContext();

		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.LOITERT));

		final LoiterTime item = (LoiterTime) this.itemRender.getMissionItem();

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context,
                MIN_ALTITUDE, MAX_ALTITUDE, "%d m");
        altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
        final CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) view.findViewById
                (R.id.altitudePicker);
        cardAltitudePicker.setViewAdapter(altitudeAdapter);
        cardAltitudePicker.setCurrentValue((int) item.getCoordinate().getAltitude().valueInMeters());
        cardAltitudePicker.addChangingListener(this);

        final NumericWheelAdapter loiterTimeAdapter = new NumericWheelAdapter(context, 0, 600, "%d s");
        loiterTimeAdapter.setItemResource(R.layout.wheel_text_centered);
        final CardWheelHorizontalView loiterTimePicker = (CardWheelHorizontalView) view.findViewById
                (R.id.loiterTimePicker);
        loiterTimePicker.setViewAdapter(loiterTimeAdapter);
        loiterTimePicker.setCurrentValue((int) item.getTime());
        loiterTimePicker.addChangingListener(this);

	}

    @Override
    public void onChanged(CardWheelHorizontalView wheel, int oldValue, int newValue) {
        final LoiterTime item = (LoiterTime) this.itemRender.getMissionItem();

        switch(wheel.getId()){
            case R.id.altitudePicker:
                item.getCoordinate().getAltitude().set(newValue);
                break;

            case R.id.loiterTimePicker:
                item.setTime(newValue);
                break;
        }
    }
}
