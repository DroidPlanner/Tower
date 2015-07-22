package org.droidplanner.android.fragments.mode;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.unit.UnitManager;
import org.w3c.dom.Text;

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
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.common.msg_command_int;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_mission_set_current;
import com.MAVLink.common.msg_set_mode;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.MAVLink.enums.MAV_GOTO;
import com.MAVLink.enums.MAV_MODE;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.drone.DroneStateApi;
import com.o3dr.android.client.apis.drone.ExperimentalApi;
import com.o3dr.android.client.apis.drone.ParameterApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.action.Action;
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
    }
    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action){
                case AttributeEvent.MISSION_ITEM_UPDATED:
                    mission = drone.getAttribute(AttributeType.MISSION);
                    int currentWaypoint = intent.getIntExtra(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, 0);
                    mission.setCurrentMissionItem(currentWaypoint);
                case AttributeEvent.GPS_POSITION:
                    updateMission();
                    break;

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

    private TextView waypointSpeed;
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
        view.findViewById(R.id.wp_speed_plus).setOnClickListener(this);
        view.findViewById(R.id.wp_speed_minus).setOnClickListener(this);
        waypointSpeed = (TextView) view.findViewById(R.id.wp_speed);
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
                if(mission.getCurrentMissionItem() == mission.getMissionItems().size()){
                    resetAutoMode();
                }
                msg_mission_set_current msg = new msg_mission_set_current();
                msg.seq = 0;
                ExperimentalApi.sendMavlinkMessage(drone, new MavlinkMessageWrapper(msg));
                break;
            }
			case R.id.mc_next: {
                msg_mission_set_current msg = new msg_mission_set_current();
                msg.seq = mission.getCurrentMissionItem() + 1;
                ExperimentalApi.sendMavlinkMessage(drone, new MavlinkMessageWrapper(msg));
                break;
            }
			case R.id.mc_prev: {
                msg_mission_set_current msg = new msg_mission_set_current();
                msg.seq = mission.getCurrentMissionItem() - 1;
                ExperimentalApi.sendMavlinkMessage(drone, new MavlinkMessageWrapper(msg));
                break;
            }
			case R.id.wp_speed_plus:
                changeWaypointSpeedBy(100.0);
				break;
			case R.id.wp_speed_minus:
                changeWaypointSpeedBy(-100.0);
				break;
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

    public void resetAutoMode(){
        new Thread(){
            public void run(){
                DroneStateApi.setVehicleMode(drone, VehicleMode.COPTER_BRAKE);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DroneStateApi.setVehicleMode(drone, VehicleMode.COPTER_AUTO);
            }
        }.start();
    }



    private void updateMission(){
        if(mission == null)
            return;
        Gps gps = drone.getAttribute(AttributeType.GPS);
        if(!gps.isValid())
            return;
        MissionProxy proxy= ((DroidPlannerApp)getActivity().getApplication()).getMissionProxy();
        int missionSize = proxy.getPathPoints().size();
        int currentItem = mission.getCurrentMissionItem();
        LatLong dronePos = gps.getPosition();
        int offset = mission.getMissionItems().size() - missionSize + 1;
        List<LatLong> remainingMission = proxy.getPathPoints().subList(Math.max(currentItem -offset,0), missionSize);
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
        waypointSpeed.setText("(" + speed/100 + "m/s)");
    }

    private void changeWaypointSpeedBy(final double delta){
        drone.getAttributeAsync(AttributeType.PARAMETERS, new Drone.OnAttributeRetrievedCallback<Parcelable>() {
            @Override
            public void onRetrievalSucceed(Parcelable parcelable) {
                Parameters params = (Parameters) parcelable;
                Parameter speed = params.getParameter(WPNAV_SPEED);
                speed.setValue(speed.getValue() + delta);
                List<Parameter> parameters = params.getParameters();
                parameters.set(parameters.indexOf(speed), speed);
                params.setParametersList(parameters);
                ParameterApi.writeParameters(drone, params);
            }

            @Override
            public void onRetrievalFailed() {

            }
        });
    }

    private void setWaypointSpeed(final double value){
        drone.getAttributeAsync(AttributeType.PARAMETERS, new Drone.OnAttributeRetrievedCallback<Parcelable>() {
            @Override
            public void onRetrievalSucceed(Parcelable parcelable) {
                Parameters params = (Parameters) parcelable;
                Parameter speed = params.getParameter(WPNAV_SPEED);
                speed.setValue(value);
                List<Parameter> parameters = params.getParameters();
                parameters.set(parameters.indexOf(speed), speed);
                params.setParametersList(parameters);
                ParameterApi.writeParameters(drone, params);
            }

            @Override
            public void onRetrievalFailed() {

            }
        });
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
