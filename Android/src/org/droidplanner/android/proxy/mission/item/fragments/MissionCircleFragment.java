package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;

import org.droidplanner.android.R;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import java.util.List;

public class MissionCircleFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_circle;
	}

	@Override
	public void onApiConnected() {
		super.onApiConnected();

		final View view = getView();
		final Context context = getActivity().getApplicationContext();

		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CIRCLE));

		final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context, MIN_ALTITUDE,
				MAX_ALTITUDE, "%d m");
		altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
		CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.altitudePicker);
		altitudePicker.setViewAdapter(altitudeAdapter);
		altitudePicker.addScrollListener(this);

		final NumericWheelAdapter loiterTurnAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 0, 10, "%d");
		CardWheelHorizontalView loiterTurnPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterTurnPicker);
		loiterTurnPicker.setViewAdapter(loiterTurnAdapter);
		loiterTurnPicker.addScrollListener(this);

		final NumericWheelAdapter loiterRadiusAdapter = new NumericWheelAdapter(context, 0, 50,
				"%d m");
		loiterRadiusAdapter.setItemResource(R.layout.wheel_text_centered);
		CardWheelHorizontalView loiterRadiusPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterRadiusPicker);
		loiterRadiusPicker.setViewAdapter(loiterRadiusAdapter);
		loiterRadiusPicker.addScrollListener(this);

		// Use the first one as reference.
		final Circle firstItem = getMissionItems().get(0);
		altitudePicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude());
		loiterTurnPicker.setCurrentValue(firstItem.getTurns());
		loiterRadiusPicker.setCurrentValue((int) firstItem.getRadius());
	}

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, int startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {

    }

    @Override
	public void onScrollingEnded(CardWheelHorizontalView cardWheel, int startValue, int endValue) {
		switch (cardWheel.getId()) {
		case R.id.altitudePicker:
			for (Circle item : getMissionItems()) {
				item.getCoordinate().setAltitude(endValue);
			}
			break;

		case R.id.loiterRadiusPicker:
			for (Circle item : getMissionItems()) {
				item.setRadius(endValue);
			}

			MissionProxy missionProxy = getMissionProxy();
			if (missionProxy != null)
				missionProxy.notifyMissionUpdate();
			break;

		case R.id.loiterTurnPicker:
			for (Circle item : getMissionItems()) {
				item.setTurns(endValue);
			}
			break;
		}
	}

	@Override
	public List<Circle> getMissionItems() {
		return (List<Circle>) super.getMissionItems();
	}
}
