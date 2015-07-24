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
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.proxy.mission.MissionProxy;

public class ModeAutoFragment extends Fragment implements View.OnClickListener{
    public static final String WPNAV_SPEED = "WPNAV_SPEED";
    private Drone drone;

    private static final IntentFilter eventFilter = new IntentFilter();
    static{
        eventFilter.addAction(AttributeEvent.MISSION_ITEM_UPDATED);
        eventFilter.addAction(AttributeEvent.PARAMETER_RECEIVED);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
    }
    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action){
                case AttributeEvent.MISSION_ITEM_UPDATED:
                    mission = drone.getAttribute(AttributeType.MISSION);
                    int currentMissionItem = intent.getIntExtra(AttributeEventExtra.EXTRA_MISSION_CURRENT_MISSION_ITEM, 0);
                    nextWaypoint = intent.getIntExtra(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, 0);
                    mission.setCurrentMissionItem(currentMissionItem);
                    break;
                case AttributeEvent.GPS_POSITION:
                    remainingMissionLength = intent.getDoubleExtra(AttributeEventExtra.EXTRA_MISSION_REMAINING_DISTANCE, 0);
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
        if(mission == null){
            mission = drone.getAttribute(AttributeType.MISSION);
        }
		switch(v.getId()){
			case R.id.mc_pause: {
                drone.pauseAtCurrentLocation();
                break;
            }
			case R.id.mc_restart: {
                gotoMissionItem(0);
                break;
            }
			case R.id.mc_next: {
                gotoMissionItem(mission.getCurrentMissionItem() + 1);
                break;
            }
			case R.id.mc_prev: {
                gotoMissionItem(mission.getCurrentMissionItem() - 1);
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
            VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_BRAKE, new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    MissionApi.getApi(drone).startMission(true, true, new AbstractCommandListener() {
                        @Override
                        public void onSuccess() {
                            MissionApi.getApi(drone).gotoMissionItem(waypoint,null);
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
            MissionApi.getApi(drone).gotoMissionItem(waypoint,null);
        }
    }



    private void updateMission(){
        if(mission == null)
            return;
        MissionProxy proxy= ((DroidPlannerApp)getActivity().getApplication()).getMissionProxy();
        double totalLength = proxy.getMissionLength();
        missionProgress.setMax((int) totalLength);
        missionProgress.setProgress((int) ((totalLength - remainingMissionLength)));
        missionFinished = remainingMissionLength < 5;
    }

    @Override
	public void onAttach(Activity activity) {
		drone = ((DroidPlannerApp)activity.getApplication()).getDrone();
		super.onAttach(activity);
	}
}
