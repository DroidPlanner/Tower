package co.aerobotics.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import org.beyene.sius.unit.length.LengthUnit;
import co.aerobotics.android.R;
import co.aerobotics.android.utils.unit.providers.length.LengthUnitProvider;
import co.aerobotics.android.view.spinnerWheel.CardWheelHorizontalView;
import co.aerobotics.android.view.spinnerWheel.adapters.LengthWheelAdapter;
import co.aerobotics.android.view.spinnerWheel.adapters.NumericWheelAdapter;

public class MissionWaypointFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_waypoint;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        final Context context = getContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.WAYPOINT));

        final Waypoint item = (Waypoint) getMissionItems().get(0);

        final NumericWheelAdapter delayAdapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 60, "%d s");
        CardWheelHorizontalView<Integer> delayPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .waypointDelayPicker);
        delayPicker.setViewAdapter(delayAdapter);
        delayPicker.setCurrentValue((int) item.getDelay());
        delayPicker.addScrollListener(this);

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));
        CardWheelHorizontalView<LengthUnit> altitudePicker = (CardWheelHorizontalView) view.findViewById(R.id
                .altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget(item.getCoordinate().getAltitude()));
        altitudePicker.addScrollListener(this);
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
                final double altitude = ((LengthUnit) endValue).toBase().getValue();
                for (MissionItem item : getMissionItems()) {
                    ((Waypoint) item).getCoordinate().setAltitude(altitude);
                }
                getMissionProxy().notifyMissionUpdate();
                break;

            case R.id.waypointDelayPicker:
                final int delay = (Integer) endValue;
                for (MissionItem item : getMissionItems()) {
                    ((Waypoint) item).setDelay(delay);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }

    }
}
