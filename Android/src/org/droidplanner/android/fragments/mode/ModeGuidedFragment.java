package org.droidplanner.android.fragments.mode;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.LengthWheelAdapter;

public class ModeGuidedFragment extends ApiListenerFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<LengthUnit>,FlightMapFragment.OnGuidedClickListener {

    private static final float DEFAULT_ALTITUDE = 2f;

    private CardWheelHorizontalView<LengthUnit> mAltitudeWheel;
    protected FlightActivity parentActivity;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof FlightActivity))
            throw new IllegalStateException("Parent activity must be an instance of " + FlightActivity.class.getName());

        parentActivity = (FlightActivity) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        parentActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mode_guided, container, false);
    }

    @Override
    public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        final LengthUnitProvider lengthUnitProvider = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(getContext(), R.layout.wheel_text_centered,
                lengthUnitProvider.boxBaseValueToTarget(2), lengthUnitProvider.boxBaseValueToTarget(200));

        mAltitudeWheel = (CardWheelHorizontalView<LengthUnit>) parentView.findViewById(R.id.altitude_spinner);
        mAltitudeWheel.setViewAdapter(altitudeAdapter);
        mAltitudeWheel.addScrollListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAltitudeWheel != null) {
            mAltitudeWheel.removeChangingListener(this);
        }
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, LengthUnit startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, LengthUnit oldValue, LengthUnit newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, LengthUnit startValue, LengthUnit endValue) {
        switch (cardWheel.getId()) {
            case R.id.altitude_spinner:
                final Drone drone = getDrone();
                if (drone.isConnected())
                    drone.setGuidedAltitude(endValue.toBase().getValue());
                break;
        }
    }

    @Override
    public void onApiConnected() {
        final Drone drone = getDrone();

        if (mAltitudeWheel != null) {
            GuidedState guidedState = drone.getAttribute(AttributeType.GUIDED_STATE);
            LatLongAlt coordinate = guidedState == null ? null : guidedState.getCoordinate();

            final LengthUnit initialValue = getLengthUnitProvider().boxBaseValueToTarget(
                    Math.max(guidedState == null
                                    ? DEFAULT_ALTITUDE
                                    : coordinate == null ? DEFAULT_ALTITUDE : coordinate.getAltitude(),
                            DEFAULT_ALTITUDE));
            mAltitudeWheel.setCurrentValue(initialValue);
        }

        parentActivity.setGuidedClickListener(this);
        Type droneType = drone.getAttribute(AttributeType.TYPE);
        if(droneType.getDroneType() == Type.TYPE_ROVER){
            mAltitudeWheel.setVisibility(View.GONE);
        }
        else{
            mAltitudeWheel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onApiDisconnected() {
        parentActivity.setGuidedClickListener(null);
    }

    @Override
    public void onGuidedClick(LatLong coord) {
        final Drone drone = getDrone();
        if(drone != null)
            drone.sendGuidedPoint(coord, false);
    }
}
