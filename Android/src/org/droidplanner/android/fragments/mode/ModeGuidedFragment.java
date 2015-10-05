package org.droidplanner.android.fragments.mode;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Type;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.FlightDataFragment;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.view.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.view.spinnerWheel.adapters.LengthWheelAdapter;

public class ModeGuidedFragment extends ApiListenerFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<LengthUnit>, FlightMapFragment.OnGuidedClickListener {

    private CardWheelHorizontalView<LengthUnit> mAltitudeWheel;
    protected FlightDataFragment parent;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        final Fragment parentFragment = getParentFragment().getParentFragment();
        if (!(parentFragment instanceof FlightDataFragment)) {
            throw new IllegalStateException("Parent fragment must be an instance of " + FlightDataFragment.class.getName());
        }

        parent = (FlightDataFragment) parentFragment;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parent = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mode_guided, container, false);
    }

    @Override
    public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        final DroidPlannerPrefs dpPrefs = getAppPrefs();

        final LengthUnitProvider lengthUnitProvider = getLengthUnitProvider();
        final LengthWheelAdapter altitudeAdapter = new LengthWheelAdapter(getContext(), R.layout.wheel_text_centered,
                lengthUnitProvider.boxBaseValueToTarget(dpPrefs.getMinAltitude()),
                lengthUnitProvider.boxBaseValueToTarget(dpPrefs.getMaxAltitude()));

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
            final DroidPlannerPrefs dpPrefs = getAppPrefs();

            final double maxAlt = dpPrefs.getMaxAltitude();
            final double minAlt = dpPrefs.getMinAltitude();
            final double defaultAlt = dpPrefs.getDefaultAltitude();

            GuidedState guidedState = drone.getAttribute(AttributeType.GUIDED_STATE);
            LatLongAlt coordinate = guidedState == null ? null : guidedState.getCoordinate();

            final double baseValue = Math.min(maxAlt,
                    Math.max(minAlt, coordinate == null ? defaultAlt : coordinate.getAltitude()));
            final LengthUnit initialValue = getLengthUnitProvider().boxBaseValueToTarget(baseValue);
            mAltitudeWheel.setCurrentValue(initialValue);
        }

        parent.setGuidedClickListener(this);
        Type droneType = drone.getAttribute(AttributeType.TYPE);
        if (droneType.getDroneType() == Type.TYPE_ROVER) {
            mAltitudeWheel.setVisibility(View.GONE);
        } else {
            mAltitudeWheel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onApiDisconnected() {
        parent.setGuidedClickListener(null);
    }

    @Override
    public void onGuidedClick(LatLong coord) {
        final Drone drone = getDrone();
        if (drone != null)
            drone.sendGuidedPoint(coord, false);
    }
}
