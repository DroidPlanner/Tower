package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.view.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.view.spinnerWheel.adapters.LengthWheelAdapter;
import org.droidplanner.android.view.spinnerWheel.adapters.NumericWheelAdapter;

public class MissionTakeoffFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_takeoff;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        final Context context = getContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.TAKEOFF));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));
        CardWheelHorizontalView<LengthUnit> cardAltitudePicker = (CardWheelHorizontalView) view
                .findViewById(R.id.altitudePicker);
        cardAltitudePicker.setViewAdapter(altitudeAdapter);
        cardAltitudePicker.addScrollListener(this);

        final NumericWheelAdapter pitchAdapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 90, "%d°");
        final CardWheelHorizontalView<Integer> pitchPicker = (CardWheelHorizontalView) view.findViewById(R.id.pitchPicker);
        pitchPicker.setViewAdapter(pitchAdapter);
        pitchPicker.addScrollListener(this);

        Takeoff item = (Takeoff) getMissionItems().get(0);
        cardAltitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget(item.getTakeoffAltitude()));
        pitchPicker.setCurrentValue((int) item.getTakeoffPitch());
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Object startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Object oldValue, Object newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, Object startValue, Object endValue) {
        switch (wheel.getId()) {
            case R.id.altitudePicker:
                final double baseValue = ((LengthUnit) endValue).toBase().getValue();
                for (MissionItem missionItem : getMissionItems()) {
                    Takeoff item = (Takeoff) missionItem;
                    item.setTakeoffAltitude(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;

            case R.id.pitchPicker:
                final int pitch = (Integer) endValue;
                for(MissionItem missionItem : getMissionItems()){
                    ((Takeoff) missionItem).setTakeoffPitch(pitch);
                }

                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
