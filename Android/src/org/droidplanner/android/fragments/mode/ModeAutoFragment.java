package org.droidplanner.android.fragments.mode;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.proxy.mission.MissionProxy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.util.MathUtils;

import java.util.List;

import timber.log.Timber;

public class ModeAutoFragment extends Fragment implements View.OnClickListener{
    public static final String WPNAV_SPEED = "WPNAV_SPEED";
    private Drone drone;

    private static final IntentFilter eventFilter = new IntentFilter();
    static{
        eventFilter.addAction(AttributeEvent.MISSION_ITEM_UPDATED);
        eventFilter.addAction(AttributeEvent.PARAMETER_RECEIVED);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(AttributeEvent.AUTOPILOT_MESSAGE);
        eventFilter.addAction(AttributeEvent.MISSION_ITEM_REACHED);
    }
    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action){
                case AttributeEvent.MISSION_ITEM_UPDATED:
                    nextWaypoint = intent.getIntExtra(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, 0);
                case AttributeEvent.GPS_POSITION:
                    updateMission();
                    break;

                case AttributeEvent.MISSION_ITEM_REACHED:
                    mission = drone.getAttribute(AttributeType.MISSION);
                    int currentMissionItem = intent.getIntExtra(AttributeEventExtra.EXTRA_MISSION_CURRENT_MISSION_ITEM, 0);
                    mission.setCurrentMissionItem(currentMissionItem);
                    updateMission();

                case AttributeEvent.PARAMETER_RECEIVED:
                    String paramName = intent.getStringExtra(AttributeEventExtra.EXTRA_PARAMETER_NAME);
                    if(paramName.equals(WPNAV_SPEED)) {
                        double value = intent.getDoubleExtra(AttributeEventExtra.EXTRA_PARAMETER_VALUE, 2000.0);
                        updateWaypointSpeed(value);
                    }
                    break;

                case AttributeEvent.AUTOPILOT_MESSAGE:
                    String message = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE);
                    Timber.i(message);
            }
        }
    };
    private Mission mission;
    private int nextWaypoint;
    private ProgressBar missionProgress;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mode_auto, container, false);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.mc_pause).setOnClickListener(this);
        view.findViewById(R.id.mc_restart).setOnClickListener(this);
        view.findViewById(R.id.mc_next).setOnClickListener(this);
        view.findViewById(R.id.mc_prev).setOnClickListener(this);
        missionProgress = (ProgressBar) view.findViewById(R.id.mission_progress);
    }

    @Override
	public void onClick(View v) {
		Parameters params = drone.getAttribute(AttributeType.PARAMETERS);
        Parameter speed = params.getParameter(WPNAV_SPEED);
        if(mission == null){
            mission = drone.getAttribute(AttributeType.MISSION);
        }
		switch(v.getId()){
			case R.id.mc_pause: {
                drone.pauseAtCurrentLocation();
                break;
            }
			case R.id.mc_restart: {
                MissionApi.getApi(drone).gotoMissionItem(0, 1, null);
                break;
            }
			case R.id.mc_next: {
                MissionApi.getApi(drone).gotoMissionItem(mission.getCurrentMissionItem() + 1, 1, null);
                break;
            }
			case R.id.mc_prev: {
                MissionApi.getApi(drone).gotoMissionItem(mission.getCurrentMissionItem() - 1, 1, null);
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



    private void updateMission(){
        if(mission == null)
            return;
        Gps gps = drone.getAttribute(AttributeType.GPS);
        if(!gps.isValid())
            return;
        MissionProxy proxy= ((DroidPlannerApp)getActivity().getApplication()).getMissionProxy();
        int missionSize = proxy.getPathPoints().size();
        LatLong dronePos = gps.getPosition();
        int offset = mission.getMissionItems().size() - missionSize + 1;
        List<LatLong> remainingMission = proxy.getPathPoints().subList(Math.max(nextWaypoint -offset,0), missionSize);
        double remainingLength = MathUtils.getDistance2D(dronePos, remainingMission.get(0));
        for (int i = 1; i < remainingMission.size(); i ++){
            remainingLength += MathUtils.getDistance2D(remainingMission.get(i-1), remainingMission.get(i));
        }
        double totalLength = proxy.getMissionLength();
        if(remainingLength > totalLength){
            remainingLength = totalLength;
        }
        missionProgress.setProgress((int)(((totalLength - remainingLength)/totalLength)*100));
    }

    private void updateWaypointSpeed(double speed) {
        Timber.i("speed set to: %f ", speed);
    }

    @Override
	public void onAttach(Activity activity) {
		drone = ((DroidPlannerApp)activity.getApplication()).getDrone();
		super.onAttach(activity);
        drone.getAttributeAsync(AttributeType.PARAMETERS, new Drone.OnAttributeRetrievedCallback<Parcelable>() {
            @Override
            public void onRetrievalSucceed(Parcelable parcelable) {
                Parameter speed = ((Parameters) parcelable).getParameter(WPNAV_SPEED);
                if (speed != null) {
                    updateWaypointSpeed(speed.getValue());
                }
            }

            @Override
            public void onRetrievalFailed() {

            }
        });

	}
}
