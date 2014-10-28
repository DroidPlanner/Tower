package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.proxy.mission.MissionProxy;
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

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_circle;
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        super.onApiConnected(api);

        final View view = getView();
        final Context context = getActivity().getApplicationContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CIRCLE));

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context, MIN_ALTITUDE,
                MAX_ALTITUDE, "%d m");
        altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
        CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view.findViewById(R.id
                .altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addChangingListener(this);

        final NumericWheelAdapter loiterTurnAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 10, "%d");
        CardWheelHorizontalView loiterTurnPicker = (CardWheelHorizontalView) view.findViewById(R.id.loiterTurnPicker);
        loiterTurnPicker.setViewAdapter(loiterTurnAdapter);
        loiterTurnPicker.addChangingListener(this);

        final NumericWheelAdapter loiterRadiusAdapter = new NumericWheelAdapter(context, 0, 50,
                "%d m");
        loiterRadiusAdapter.setItemResource(R.layout.wheel_text_centered);
        CardWheelHorizontalView loiterRadiusPicker = (CardWheelHorizontalView) view.findViewById(R.id.loiterRadiusPicker);
        loiterRadiusPicker.setViewAdapter(loiterRadiusAdapter);
        loiterRadiusPicker.addChangingListener(this);

        //Use the first one as reference.
        final Circle firstItem = getMissionItems().get(0);
        altitudePicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude().valueInMeters
                ());
        loiterTurnPicker.setCurrentValue(firstItem.getNumberOfTurns());
        loiterRadiusPicker.setCurrentValue((int) firstItem.getRadius());
    }

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.altitudePicker:
            for(Circle item: getMissionItems()) {
                item.setAltitude(new Altitude(newValue));
            }
			break;

		case R.id.loiterRadiusPicker:
            for(Circle item: getMissionItems()) {
                item.setRadius(newValue);
            }

            MissionProxy missionProxy = getMissionProxy();
            if(missionProxy != null)
                missionProxy.getMission().notifyMissionUpdate();
			break;

		case R.id.loiterTurnPicker:
            for(Circle item: getMissionItems()) {
                item.setTurns(newValue);
            }
			break;
		}
	}

    @Override
    public List<Circle> getMissionItems(){
        return (List<Circle>) super.getMissionItems();
    }
}
