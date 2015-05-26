package org.droidplanner.android.proxy.mission.item.fragments;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.LengthWheelAdapter;

public class MissionTakeoffFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<LengthUnit> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_takeoff;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.TAKEOFF));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(getContext(), R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));
        CardWheelHorizontalView<LengthUnit> cardAltitudePicker = (CardWheelHorizontalView) getView()
                .findViewById(R.id.altitudePicker);
        cardAltitudePicker.setViewAdapter(altitudeAdapter);
        cardAltitudePicker.addScrollListener(this);

        Takeoff item = (Takeoff) getMissionItems().get(0);
        cardAltitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget(item.getTakeoffAltitude()));
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, LengthUnit startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, LengthUnit oldValue, LengthUnit newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, LengthUnit startValue, LengthUnit endValue) {
        switch (wheel.getId()) {
            case R.id.altitudePicker:
                final double baseValue = endValue.toBase().getValue();
                for (MissionItem missionItem : getMissionItems()) {
                    Takeoff item = (Takeoff) missionItem;
                    item.setTakeoffAltitude(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
