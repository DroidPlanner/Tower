package org.droidplanner.android.fragments.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.DroneMap;
import org.droidplanner.android.graphic.map.GuidedScanROIMarkerInfo;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.view.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.view.spinnerWheel.adapters.LengthWheelAdapter;

public class ModeFollowFragment extends ModeGuidedFragment implements OnItemSelectedListener {

    private static final double DEFAULT_MIN_RADIUS = 2; //meters

    private static final IntentFilter eventFilter = new IntentFilter(AttributeEvent.FOLLOW_UPDATE);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.FOLLOW_UPDATE:
                    final FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
                    if (followState != null) {
                        final FollowType followType = followState.getMode();
                        onFollowTypeUpdate(followType, followState.getParams());
                    }
                    break;
            }
        }
    };

    private final GuidedScanROIMarkerInfo roiMarkerInfo = new GuidedScanROIMarkerInfo();

    private FollowType lastFollowType;
    private Bundle lastFollowParams;

    private TextView modeDescription;
    private Spinner spinner;
    private ArrayAdapter<FollowType> adapter;

    private CardWheelHorizontalView<LengthUnit> mRadiusWheel;
    private CardWheelHorizontalView<LengthUnit> roiHeightWheel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mode_follow, container, false);
    }

    @Override
    public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        modeDescription = (TextView) parentView.findViewById(R.id.ModeDetail);

        final Context context = getContext();
        final LengthUnitProvider lengthUP = getLengthUnitProvider();

        final DroidPlannerPrefs dpPrefs = getAppPrefs();

        final LengthWheelAdapter radiusAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(Utils.MIN_DISTANCE), lengthUP.boxBaseValueToTarget(Utils.MAX_DISTANCE));

        mRadiusWheel = (CardWheelHorizontalView<LengthUnit>) parentView.findViewById(R.id.radius_spinner);
        mRadiusWheel.setViewAdapter(radiusAdapter);
        mRadiusWheel.addScrollListener(this);

        final LengthWheelAdapter roiHeightAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(dpPrefs.getMinAltitude()), lengthUP.boxBaseValueToTarget(dpPrefs.getMaxAltitude()));

        roiHeightWheel = (CardWheelHorizontalView<LengthUnit>) parentView.findViewById(R.id.roi_height_spinner);
        roiHeightWheel.setViewAdapter(roiHeightAdapter);
        roiHeightWheel.addScrollListener(this);

        spinner = (Spinner) parentView.findViewById(R.id.follow_type_spinner);
        adapter = new FollowTypesAdapter(context, false);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mRadiusWheel != null) {
            mRadiusWheel.removeChangingListener(this);
        }
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
        if (followState != null) {
            final FollowType followType = followState.getMode();
            onFollowTypeUpdate(followType, followState.getParams());
        }

        parent.addMarker(roiMarkerInfo);
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    private void onFollowTypeUpdate(FollowType followType, Bundle params) {
        Context context = getContext();
        if(followType == null || context == null) {
            return;
        }

        if(followType != lastFollowType) {
            lastFollowType = followType;

            spinner.setSelection(adapter.getPosition(followType));
            updateModeDescription(followType);
        }

        if(params != null){
            params.setClassLoader(context.getClassLoader());
        }

        if(!Utils.equalBundles(params, lastFollowParams)) {
            lastFollowParams = params;

            if (followType.hasParam(FollowType.EXTRA_FOLLOW_RADIUS)) {
                double radius = DEFAULT_MIN_RADIUS;
                if (params != null) {
                    radius = params.getDouble(FollowType.EXTRA_FOLLOW_RADIUS, DEFAULT_MIN_RADIUS);
                }

                mRadiusWheel.setVisibility(View.VISIBLE);
                mRadiusWheel.setCurrentValue((getLengthUnitProvider().boxBaseValueToTarget(radius)));
            } else {
                mRadiusWheel.setVisibility(View.GONE);
            }

            double roiHeight = GuidedScanROIMarkerInfo.DEFAULT_FOLLOW_ROI_ALTITUDE;
            LatLong roiTarget = null;
            if (followType.hasParam(FollowType.EXTRA_FOLLOW_ROI_TARGET)) {
                roiTarget = roiMarkerInfo.getPosition();

                if (params != null) {
                    params.setClassLoader(LatLong.class.getClassLoader());
                    roiTarget = params.getParcelable(FollowType.EXTRA_FOLLOW_ROI_TARGET);
                }

                if (roiTarget instanceof LatLongAlt)
                    roiHeight = ((LatLongAlt) roiTarget).getAltitude();
            }

            roiHeightWheel.setCurrentValue(getLengthUnitProvider().boxBaseValueToTarget(roiHeight));
            updateROITargetMarker(roiTarget);
        }
    }

    private void updateModeDescription(FollowType followType) {
        if(followType == null)
            return;

        switch (followType) {
            case GUIDED_SCAN:
                modeDescription.setText(R.string.mode_follow_guided_scan);
                break;

            default:
                modeDescription.setText(R.string.mode_follow);
                break;
        }
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        parent.removeMarker(roiMarkerInfo);
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, LengthUnit oldValue, LengthUnit newValue) {
        final Drone drone = getDrone();
        switch (cardWheel.getId()) {
            case R.id.radius_spinner:
                if (drone.isConnected()) {
                    Bundle params = new Bundle();
                    params.putDouble(FollowType.EXTRA_FOLLOW_RADIUS, newValue.toBase().getValue());
                    FollowApi.getApi(drone).updateFollowParams(params);
                }
                break;

            case R.id.roi_height_spinner:
                if (drone.isConnected()) {
                    final LatLongAlt roiCoord = roiMarkerInfo.getPosition();
                    if (roiCoord != null) {
                        roiCoord.setAltitude(newValue.toBase().getValue());
                        pushROITargetToVehicle(drone, roiCoord);
                    }
                }
                break;

            default:
                super.onScrollingEnded(cardWheel, oldValue, newValue);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final FollowType type = adapter.getItem(position);

        getAppPrefs().setLastKnownFollowType(type);

        final Drone drone = getDrone();
        if (drone.isConnected()) {
            FollowApi.getApi(drone).enableFollowMe(type);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onGuidedClick(LatLong coord) {
        final Drone drone = getDrone();
        final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
        if (followState != null && followState.isEnabled() && followState.getMode().hasParam(FollowType.EXTRA_FOLLOW_ROI_TARGET)) {
            Toast.makeText(getContext(), R.string.guided_scan_roi_set_message, Toast.LENGTH_LONG).show();

            final double roiHeight = roiHeightWheel.getCurrentValue().toBase().getValue();
            final LatLongAlt roiCoord = new LatLongAlt(coord.getLatitude(), coord.getLongitude(), roiHeight);

            pushROITargetToVehicle(drone, roiCoord);
            updateROITargetMarker(coord);
        } else {
            super.onGuidedClick(coord);
        }
    }

    private void pushROITargetToVehicle(Drone drone, LatLongAlt roiCoord) {
        if (roiCoord == null)
            return;

        Bundle params = new Bundle();
        params.putParcelable(FollowType.EXTRA_FOLLOW_ROI_TARGET, roiCoord);
        FollowApi.getApi(drone).updateFollowParams(params);
    }

    private void updateROITargetMarker(LatLong target) {
        roiMarkerInfo.setPosition(target);
        getBroadcastManager().sendBroadcast(new Intent(DroneMap.ACTION_UPDATE_MAP));

        if (target == null) {
            roiHeightWheel.setVisibility(View.GONE);
        } else {
            roiHeightWheel.setVisibility(View.VISIBLE);
        }
    }

    private static class FollowTypesAdapter extends ArrayAdapter<FollowType> {

        private final LayoutInflater inflater;

        public FollowTypesAdapter(Context context, boolean isAdvancedMenuEnabled) {
            super(context, 0, FollowType.getFollowTypes(isAdvancedMenuEnabled));
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView == null) {
                view = (TextView) inflater.inflate(R.layout.list_item_follow_types, parent, false);
            } else {
                view = (TextView) convertView;
            }

            final FollowType followType = getItem(position);
            view.setText(followType.getTypeLabel());
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }
}
