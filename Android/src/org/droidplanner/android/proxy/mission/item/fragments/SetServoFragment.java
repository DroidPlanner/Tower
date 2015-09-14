package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.SetServo;

import org.droidplanner.android.R;
import org.droidplanner.android.view.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.view.spinnerWheel.adapters.NumericWheelAdapter;

public class SetServoFragment extends MissionDetailFragment implements CardWheelHorizontalView
        .OnCardWheelScrollListener<Integer> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_set_servo;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SET_SERVO));

        final SetServo item = (SetServo) getMissionItems().get(0);
        final Context context = getContext();

        final NumericWheelAdapter adapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, 1, 8, "%d");
        final CardWheelHorizontalView<Integer> cardChannelPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .picker1);
        cardChannelPicker.setViewAdapter(adapter);
        cardChannelPicker.addScrollListener(this);
        cardChannelPicker.setCurrentValue(item.getChannel());

        final NumericWheelAdapter pwmAdapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0,
                2000, "%d");
        final CardWheelHorizontalView<Integer> pwmPicker = (CardWheelHorizontalView) view.findViewById(R.id.pwmPicker);
        pwmPicker.setViewAdapter(pwmAdapter);
        pwmPicker.addScrollListener(this);
        pwmPicker.setCurrentValue(item.getPwm());
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Integer startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Integer oldValue, Integer newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, Integer startValue, Integer endValue) {
        switch (wheel.getId()) {
            case R.id.picker1:
                for (MissionItem missionItem : getMissionItems()) {
                    SetServo item = (SetServo) missionItem;
                    item.setChannel(endValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;

            case R.id.pwmPicker:
                for (MissionItem missionItem : getMissionItems()) {
                    SetServo item = (SetServo) missionItem;
                    item.setPwm(endValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
