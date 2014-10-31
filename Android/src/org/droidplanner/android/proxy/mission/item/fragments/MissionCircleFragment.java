package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.Circle;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class MissionCircleFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener{

	private List<Circle> mItemsList;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_circle;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Context context = getActivity().getApplicationContext();

		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CIRCLE));

		mItemsList = (List<Circle>) getMissionItems();

        //Use the first one as reference.
        final Circle firstItem = mItemsList.get(0);

		final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context, MIN_ALTITUDE,
				MAX_ALTITUDE, "%d m");
		altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.altitudePicker);
		altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addChangingListener(this);
		altitudePicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude().valueInMeters
                ());

		final NumericWheelAdapter loiterTurnAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 0, 10, "%d");
		final CardWheelHorizontalView loiterTurnPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterTurnPicker);
		loiterTurnPicker.setViewAdapter(loiterTurnAdapter);
        loiterTurnPicker.addChangingListener(this);
		loiterTurnPicker.setCurrentValue(firstItem.getNumberOfTurns());

		final NumericWheelAdapter loiterRadiusAdapter = new NumericWheelAdapter(context, 0, 50,
				"%d m");
		loiterRadiusAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView loiterRadiusPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterRadiusPicker);
		loiterRadiusPicker.setViewAdapter(loiterRadiusAdapter);
        loiterRadiusPicker.addChangingListener(this);
		loiterRadiusPicker.setCurrentValue((int) firstItem.getRadius());
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.altitudePicker:
            for(Circle item: mItemsList) {
                item.setAltitude(new Altitude(newValue));
            }
			break;

		case R.id.loiterRadiusPicker:
            for(Circle item: mItemsList) {
                item.setRadius(newValue);
            }
            getMissionProxy().getMission().notifyMissionUpdate();
			break;

		case R.id.loiterTurnPicker:
            for(Circle item: mItemsList) {
                item.setTurns(newValue);
            }
			break;
		}
	}
}
