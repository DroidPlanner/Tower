package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.view.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.view.spinnerWheel.adapters.LengthWheelAdapter;
import org.droidplanner.android.view.spinnerWheel.adapters.NumericWheelAdapter;

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

        final LengthUnitProvider lengthUP = getLengthUnitProvider();

        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE));

        CardWheelHorizontalView<LengthUnit> altitudePicker = (CardWheelHorizontalView<LengthUnit>) view
                .findViewById(R.id.altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addScrollListener(this);

        final NumericWheelAdapter loiterTurnAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 50, "%d");

        CardWheelHorizontalView<Integer> loiterTurnPicker = (CardWheelHorizontalView<Integer>) view
                .findViewById(R.id.loiterTurnPicker);
        loiterTurnPicker.setViewAdapter(loiterTurnAdapter);
        loiterTurnPicker.addScrollListener(this);

        final LengthWheelAdapter loiterRadiusAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(Utils.MIN_DISTANCE), lengthUP.boxBaseValueToTarget(Utils.MAX_RADIUS));
        CardWheelHorizontalView<LengthUnit> loiterRadiusPicker = (CardWheelHorizontalView<LengthUnit>) view
                .findViewById(R.id.loiterRadiusPicker);
        loiterRadiusPicker.setViewAdapter(loiterRadiusAdapter);
        loiterRadiusPicker.addScrollListener(this);

        // Use the first one as reference.
        final Circle firstItem = getMissionItems().get(0);
        altitudePicker.setCurrentValue(lengthUP.boxBaseValueToTarget(firstItem.getCoordinate().getAltitude()));
        loiterTurnPicker.setCurrentValue(firstItem.getTurns());
        loiterRadiusPicker.setCurrentValue(lengthUP.boxBaseValueToTarget(firstItem.getRadius()));
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Object startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Object oldValue, Object newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, Object startValue, Object endValue) {
        switch (cardWheel.getId()) {
            case R.id.altitudePicker: {
                final double baseValue = ((LengthUnit) endValue).toBase().getValue();
                for (Circle item : getMissionItems()) {
                    item.getCoordinate().setAltitude(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
            }

            case R.id.loiterRadiusPicker: {
                final double baseValue =  ((LengthUnit) endValue).toBase().getValue();
                for (Circle item : getMissionItems()) {
                    item.setRadius(baseValue);
                }

                MissionProxy missionProxy = getMissionProxy();
                if (missionProxy != null)
                    missionProxy.notifyMissionUpdate();
                break;
            }

            case R.id.loiterTurnPicker:
                int turns = (Integer) endValue;
                for (Circle item : getMissionItems()) {
                    item.setTurns(turns);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }

    @Override
    public List<Circle> getMissionItems() {
        return (List<Circle>) super.getMissionItems();
    }
}
