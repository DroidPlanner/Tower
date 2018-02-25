package co.aerobotics.android.proxy.mission.item.fragments;

import android.view.View;

import co.aerobotics.android.utils.Utils;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.CameraTrigger;

import org.beyene.sius.unit.length.LengthUnit;
import co.aerobotics.android.R;
import co.aerobotics.android.utils.unit.providers.length.LengthUnitProvider;
import co.aerobotics.android.view.spinnerWheel.CardWheelHorizontalView;
import co.aerobotics.android.view.spinnerWheel.adapters.LengthWheelAdapter;

public class MissionCameraTriggerFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<LengthUnit> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_camera_trigger;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CAMERA_TRIGGER));

        CameraTrigger item = (CameraTrigger) getMissionItems().get(0);

        final LengthUnitProvider lengthUnitProvider = getLengthUnitProvider();
        final LengthWheelAdapter adapter = new LengthWheelAdapter(getContext(), R.layout.wheel_text_centered,
                lengthUnitProvider.boxBaseValueToTarget(Utils.MIN_DISTANCE),
                lengthUnitProvider.boxBaseValueToTarget(Utils.MAX_DISTANCE));
        final CardWheelHorizontalView<LengthUnit> cardAltitudePicker = (CardWheelHorizontalView<LengthUnit>) view
                .findViewById(R.id.picker1);
        cardAltitudePicker.setViewAdapter(adapter);
        cardAltitudePicker.addScrollListener(this);
        cardAltitudePicker.setCurrentValue(lengthUnitProvider.boxBaseValueToTarget(item.getTriggerDistance()));
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
            case R.id.picker1:
                double baseValue = endValue.toBase().getValue();
                for (MissionItem missionItem : getMissionItems()) {
                    CameraTrigger item = (CameraTrigger) missionItem;
                    item.setTriggerDistance(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
