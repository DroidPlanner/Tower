package co.aerobotics.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import co.aerobotics.android.proxy.mission.MissionProxy;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.DoJump;

import co.aerobotics.android.view.spinnerWheel.CardWheelHorizontalView;
import co.aerobotics.android.view.spinnerWheel.adapters.NumericWheelAdapter;

/**
 * Created by Toby on 7/31/2015.
 */
public class MissionDoJumpFragment extends MissionDetailFragment implements CardWheelHorizontalView.OnCardWheelScrollListener<Integer> {

    @Override
    protected int getResource() {
        return co.aerobotics.android.R.layout.fragment_editor_detail_do_jump;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.DO_JUMP));

        final DoJump item = (DoJump) getMissionItems().get(0);
        final Context context = getContext();

        final MissionProxy missionProxy = getMissionProxy();
        final NumericWheelAdapter adapter = new NumericWheelAdapter(context, co.aerobotics.android.R.layout.wheel_text_centered,
                missionProxy.getFirstWaypoint(), missionProxy.getLastWaypoint(), "%d");
        final CardWheelHorizontalView<Integer> waypointPicker = (CardWheelHorizontalView) view.findViewById(co.aerobotics.android.R.id.waypoint_picker);
        waypointPicker.setViewAdapter(adapter);
        waypointPicker.addScrollListener(this);
        waypointPicker.setCurrentValue(item.getWaypoint());

        final int repeatCount = item.getRepeatCount();
        final boolean isIndefinite = repeatCount == -1;

        final NumericWheelAdapter repeatCountAdapter = new NumericWheelAdapter(context, co.aerobotics.android.R.layout.wheel_text_centered, 0,
                1337, "%d");
        final CardWheelHorizontalView<Integer> repeatCountPicker = (CardWheelHorizontalView) view.findViewById(co.aerobotics.android.R.id.repeat_picker);
        repeatCountPicker.setViewAdapter(repeatCountAdapter);
        repeatCountPicker.addScrollListener(this);
        repeatCountPicker.setCurrentValue(isIndefinite ? 0 : repeatCount);
        repeatCountPicker.setVisibility(isIndefinite ? View.GONE : View.VISIBLE);

        final CheckBox indefiniteRepeatCheck = (CheckBox) view.findViewById(co.aerobotics.android.R.id.repeat_indefinitely_toggle);
        indefiniteRepeatCheck.setChecked(isIndefinite);
        indefiniteRepeatCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final int newRepeatCount = isChecked ? -1 : repeatCountPicker.getCurrentValue();
                repeatCountPicker.setVisibility(isChecked ? View.GONE : View.VISIBLE);

                for(MissionItem missionItem : getMissionItems()){
                    final DoJump item = (DoJump) missionItem;
                    item.setRepeatCount(newRepeatCount);
                }

                getMissionProxy().notifyMissionUpdate();
            }
        });
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
            case co.aerobotics.android.R.id.waypoint_picker:
                for (MissionItem missionItem : getMissionItems()) {
                    DoJump item = (DoJump) missionItem;
                    item.setWaypoint(endValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;

            case co.aerobotics.android.R.id.repeat_picker:
                for (MissionItem missionItem : getMissionItems()) {
                    DoJump item = (DoJump) missionItem;
                    item.setRepeatCount(endValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
