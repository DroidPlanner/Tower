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
import com.o3dr.android.client.apis.gcs.FollowApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.DroneMap;
import org.droidplanner.android.graphic.map.GuidedScanROIMarkerInfo;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.LengthWheelAdapter;

public class ModeFollowFragment extends ModeGuidedFragment implements OnItemSelectedListener, DroneMap.MapMarkerProvider {

    private static final int ROI_TARGET_MARKER_INDEX = 0;

    private static final IntentFilter eventFilter = new IntentFilter(AttributeEvent.FOLLOW_UPDATE);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (AttributeEvent.FOLLOW_UPDATE.equals(action)) {
                final FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
                if (followState != null) {
                    spinner.setSelection(adapter.getPosition(followState.getMode()));
                }
            }
        }
    };

    private final MarkerInfo[] markers = new MarkerInfo[1];

    {
        markers[ROI_TARGET_MARKER_INDEX] = new GuidedScanROIMarkerInfo();
    }

    private TextView modeDescription;
    private Spinner spinner;
    private ArrayAdapter<FollowType> adapter;

    private CardWheelHorizontalView<LengthUnit> mRadiusWheel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mode_follow, container, false);
    }

    @Override
    public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        final Context context = getContext();
        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        final LengthWheelAdapter radiusAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(2), lengthUP.boxBaseValueToTarget(200));

        modeDescription = (TextView) parentView.findViewById(R.id.ModeDetail);

        mRadiusWheel = (CardWheelHorizontalView<LengthUnit>) parentView.findViewById(R.id.radius_spinner);
        mRadiusWheel.setViewAdapter(radiusAdapter);
        mRadiusWheel.addScrollListener(this);

        spinner = (Spinner) parentView.findViewById(R.id.follow_type_spinner);
        adapter = new FollowTypesAdapter(context, getAppPrefs().isAdvancedMenuEnabled());
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
        if(followState != null){
            final FollowType followType = followState.getMode();
            spinner.setSelection(adapter.getPosition(followType));
            onFollowTypeUpdate(followType, followState.getParams());
        }

        parentActivity.addMapMarkerProvider(this);
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    private void onFollowTypeUpdate(FollowType followType, Bundle params){
        if (followType.hasParam(FollowType.EXTRA_FOLLOW_RADIUS)) {
            showRadiusPicker();
            updateCurrentRadius();
        } else {
            hideRadiusPicker();
        }

        if(!followType.hasParam(FollowType.EXTRA_FOLLOW_ROI_TARGET))
            markers[ROI_TARGET_MARKER_INDEX].setPosition(null);
        else if(params != null){
            params.setClassLoader(LatLong.class.getClassLoader());
            LatLong roiTarget = params.getParcelable(FollowType.EXTRA_FOLLOW_ROI_TARGET);
            if(roiTarget != null){
                updateROITargetMarker(roiTarget);
            }
        }
    }

    private void updateModeDescription(FollowType followType){
        switch(followType){
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
        parentActivity.removeMapMarkerProvider(this);
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, LengthUnit oldValue, LengthUnit newValue) {
        switch (cardWheel.getId()) {
            case R.id.radius_spinner:
                final Drone drone = getDrone();
                if (drone.isConnected()) {
                    Bundle params = new Bundle();
                    params.putDouble(FollowType.EXTRA_FOLLOW_RADIUS, newValue.toBase().getValue());
                    FollowApi.updateFollowParams(drone, params);
                }
                break;

            default:
                super.onScrollingEnded(cardWheel, oldValue, newValue);
                break;
        }
    }

    private void updateCurrentRadius() {
        final Drone drone = getDrone();
        if (mRadiusWheel != null && drone.isConnected()) {
            final FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
            Bundle params = followState.getParams();
            double radius = params.getDouble(FollowType.EXTRA_FOLLOW_RADIUS, 2);
            mRadiusWheel.setCurrentValue((getLengthUnitProvider().boxBaseValueToTarget(radius)));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final FollowType type = adapter.getItem(position);

        final Drone drone = getDrone();
        if (drone.isConnected()) {
            drone.enableFollowMe(type);
        }

        onFollowTypeUpdate(type, null);
    }

    private void hideRadiusPicker() {
        mRadiusWheel.setVisibility(View.GONE);
    }

    private void showRadiusPicker() {
        mRadiusWheel.setVisibility(View.VISIBLE);
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

            Bundle params = new Bundle();
            params.putParcelable(FollowType.EXTRA_FOLLOW_ROI_TARGET, coord);
            FollowApi.updateFollowParams(drone, params);
            updateROITargetMarker(coord);
        } else {
            super.onGuidedClick(coord);
        }
    }

    private void updateROITargetMarker(LatLong target){
        markers[ROI_TARGET_MARKER_INDEX].setPosition(target);
        getBroadcastManager().sendBroadcast(new Intent(DroneMap.ACTION_UPDATE_MAP));
    }

    @Override
    public MarkerInfo[] getMapMarkers() {
        return markers;
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
