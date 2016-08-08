package org.droidplanner.android.fragments.mode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.view.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.view.spinnerWheel.adapters.NumericWheelAdapter;

import java.util.ArrayList;
import java.util.List;

public class ModeAutoFragment extends Fragment implements View.OnClickListener, CardWheelHorizontalView.OnCardWheelScrollListener<Integer> {
    private Drone drone;

    private static final IntentFilter eventFilter = new IntentFilter();
    static{
        eventFilter.addAction(AttributeEvent.MISSION_ITEM_UPDATED);
        eventFilter.addAction(AttributeEvent.PARAMETER_RECEIVED);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(AttributeEvent.MISSION_UPDATED);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
    }
    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action){
                case AttributeEvent.MISSION_RECEIVED:
                case AttributeEvent.MISSION_UPDATED:
                    final MissionProxy missionProxy = getMissionProxy();
                    if(missionProxy != null) {
                        mission = drone.getAttribute(AttributeType.MISSION);
                        waypointSelectorAdapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered,
                                missionProxy.getFirstWaypoint(), missionProxy.getLastWaypoint(), "%3d");
                        waypointSelector.setViewAdapter(waypointSelectorAdapter);
                    }
                    break;

                case AttributeEvent.MISSION_ITEM_UPDATED:
                    mission = drone.getAttribute(AttributeType.MISSION);
                    nextWaypoint = intent.getIntExtra(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, 0);
                    waypointSelector.setCurrentValue(nextWaypoint);
                    break;
                case AttributeEvent.GPS_POSITION:
                    updateMission();
                    break;
            }
        }
    };
    private Mission mission;
    private int nextWaypoint;
    private ProgressBar missionProgress;
    private double remainingMissionLength;
    private boolean missionFinished;
    private CardWheelHorizontalView<Integer> waypointSelector;
    private NumericWheelAdapter waypointSelectorAdapter;


    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mode_auto, container, false);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.mc_pause).setOnClickListener(this);
        view.findViewById(R.id.mc_restart).setOnClickListener(this);
        missionProgress = (ProgressBar) view.findViewById(R.id.mission_progress);
        waypointSelector = (CardWheelHorizontalView<Integer>) view.findViewById(R.id.waypoint_selector);
        waypointSelector.addScrollListener(this);
        mission = drone.getAttribute(AttributeType.MISSION);

        final MissionProxy missionProxy = getMissionProxy();
        waypointSelectorAdapter = new NumericWheelAdapter(getActivity().getApplicationContext(),
                R.layout.wheel_text_centered,
                missionProxy.getFirstWaypoint(), missionProxy.getLastWaypoint(), "%3d");
        waypointSelector.setViewAdapter(waypointSelectorAdapter);
    }

    private MissionProxy getMissionProxy(){
        final Activity activity = getActivity();
        if(activity == null)
            return null;

        return ((DroidPlannerApp) activity.getApplication()).getMissionProxy();
    }

    @Override
	public void onClick(View v) {
        if(mission == null){
            mission = drone.getAttribute(AttributeType.MISSION);
        }
		switch(v.getId()){
			case R.id.mc_pause: {
                ControlApi.getApi(drone).pauseAtCurrentLocation(null);
                break;
            }
			case R.id.mc_restart: {
                gotoMissionItem(0);
                break;
            }
		}
	}

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(eventReceiver);
    }

    private void gotoMissionItem(final int waypoint){
        if(missionFinished || waypoint == 0){
            VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_GUIDED, new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    MissionApi.getApi(drone).startMission(true, true, new AbstractCommandListener() {
                        @Override
                        public void onSuccess() {
                            MissionApi.getApi(drone).gotoWaypoint(waypoint, null);
                        }

                        @Override
                        public void onError(int i) {

                        }

                        @Override
                        public void onTimeout() {

                        }
                    });
                    missionFinished = false;
                }

                @Override
                public void onError(int i) {}

                @Override
                public void onTimeout() {}
            });
        }else{
            MissionApi.getApi(drone).gotoWaypoint(waypoint, null);
        }
    }

        private double getRemainingMissionLength(){
        Gps gps = drone.getAttribute(AttributeType.GPS);
        if(mission == null || mission.getMissionItems().size() == 0 || gps == null || !gps.isValid())
            return -1;
        LatLong dronePos = gps.getPosition();
        List<MissionItem> missionItems = mission.getMissionItems();
        List<LatLong> path = new ArrayList<LatLong>();
        path.add(dronePos);
        for(int i = Math.max(nextWaypoint - 1, 0); i < missionItems.size(); i++){
            MissionItem item = missionItems.get(i);
            if(item instanceof MissionItem.SpatialItem){
                MissionItem.SpatialItem spatialItem = (MissionItem.SpatialItem)item;
                LatLongAlt coordinate = spatialItem.getCoordinate();
                path.add(new LatLong(coordinate.getLatitude(), coordinate.getLongitude()));
            }

        }
        return MathUtils.getPolylineLength(path);
    }

    private double getTotalMissionLength(){
        List<MissionItem> missionItems = mission.getMissionItems();
        List<LatLong> path = new ArrayList<LatLong>();
        for(int i = 0; i < missionItems.size(); i++){
            MissionItem item = missionItems.get(i);
            if(item instanceof MissionItem.SpatialItem){
                MissionItem.SpatialItem spatialItem = (MissionItem.SpatialItem)item;
                LatLongAlt coordinate = spatialItem.getCoordinate();
                path.add(new LatLong(coordinate.getLatitude(), coordinate.getLongitude()));
            }

        }
        return MathUtils.getPolylineLength(path);
    }



    private void updateMission(){
        if(mission == null)
            return;
        double totalLength = getTotalMissionLength();
        missionProgress.setMax((int) totalLength);
        remainingMissionLength = getRemainingMissionLength();
        missionProgress.setProgress((int) ((totalLength - remainingMissionLength)));
        missionFinished = remainingMissionLength < 5;
    }

    @Override
	public void onAttach(Activity activity) {
		drone = ((DroidPlannerApp)activity.getApplication()).getDrone();
		super.onAttach(activity);
	}

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Integer startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Integer oldValue, Integer newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, Integer startValue, Integer endValue) {
        if(cardWheel.getId() == R.id.waypoint_selector) {
            gotoMissionItem(endValue);
        }
    }
}
