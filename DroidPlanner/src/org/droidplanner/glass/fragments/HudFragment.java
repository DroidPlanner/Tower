package org.droidplanner.glass.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.Messages.ApmModes;

import org.droidplanner.MAVLink.MavLinkArm;
import org.droidplanner.R;
import org.droidplanner.activities.helpers.SuperActivity;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneEvents;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.drone.variables.Altitude;
import org.droidplanner.drone.variables.Battery;
import org.droidplanner.drone.variables.GPS;
import org.droidplanner.drone.variables.Orientation;
import org.droidplanner.drone.variables.Speed;
import org.droidplanner.glass.utils.hud.HUD;

import java.util.List;

public class HudFragment extends Fragment implements DroneInterfaces.OnDroneListener{

	private HUD hudWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.fragment_attitude_indicator, container, false);
		hudWidget = (HUD) view.findViewById(R.id.hudWidget);
		return view;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_glass_hud, menu);

        final Drone drone = ((SuperActivity) getActivity()).app.drone;

        //Fill the flight modes menu with all the implemented flight modes
        MenuItem flightModes = menu.findItem(R.id.menu_flight_modes);
        SubMenu flightModesMenu = flightModes.getSubMenu();

        //Get the list of apm modes for this drone
        List<ApmModes> apmModesList = ApmModes.getModeList(drone.type.getType());

        //Add them to the flight modes menu
        for (ApmModes apmMode : apmModesList) {
            flightModesMenu.add(apmMode.getName());
        }

        final boolean isDroneConnected = drone.MavClient.isConnected();

        //Make the drone control menu visible if connected
        menu.setGroupVisible(R.id.menu_group_drone_connected, isDroneConnected);
        menu.setGroupEnabled(R.id.menu_group_drone_connected, isDroneConnected);

        //Update the drone arming state if connected
        if(isDroneConnected){
            final MenuItem armingMenuItem = menu.findItem(R.id.menu_arming_state);
            if(armingMenuItem != null){
                boolean isArmed = drone.state.isArmed();
                armingMenuItem.setTitle(isArmed ? R.string.menu_disarm : R.string.menu_arm);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case Menu.NONE: {
                final Drone drone = ((SuperActivity) getActivity()).app.drone;

                //Handle the flight modes
                final String itemTitle = item.getTitle().toString();
                final ApmModes selectedMode = ApmModes.getMode(itemTitle, drone.type.getType());
                if (ApmModes.isValid(selectedMode)) {
                    drone.state.changeFlightMode(selectedMode);
                    return true;
                }

                return false;
            }

            case R.id.menu_arming_state:
                toggleArming();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleArming(){
        final Drone drone = ((SuperActivity) getActivity()).app.drone;
        final boolean isDroneArmed = drone.state.isArmed();
        if (!isDroneArmed){
            drone.tts.speak("Arming the vehicle, please standby");
        }

        MavLinkArm.sendArmMessage(drone, !isDroneArmed);
    }

    @Override
    public void onStart(){
        super.onStart();

        final Drone drone = ((SuperActivity) getActivity()).app.drone;
        drone.events.addDroneListener(this);

        //Check if we're connected to the drone
        hudWidget.setEnabled(drone.MavClient.isConnected());
    }

    @Override
    public void onStop(){
        super.onStop();
        final DroneEvents droneEvents = ((SuperActivity) getActivity()).app.drone.events;
        droneEvents.removeDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch(event){
            case ARMING:
            case MODE:
                hudWidget.updateDroneState(drone.state);
                break;

            case BATTERY:
                hudWidget.updateBatteryInfo(drone.battery);
                break;

            case CONNECTED:
                //Enable the hud view
                hudWidget.setEnabled(true);
                break;

            case DISCONNECTED:
                //Disable the hud view
                hudWidget.setEnabled(false);
                break;

            case GPS:
            case GPS_COUNT:
            case GPS_FIX:
                hudWidget.updateGpsInfo(drone.GPS);
                break;

            case ORIENTATION:
                //Update yaw, pitch, and roll
                hudWidget.updateOrientation(drone.orientation);
                break;

            case SPEED:
                hudWidget.updateAltitudeAndSpeed(drone.altitude, drone.speed);
                break;

            case TYPE:
                hudWidget.setDroneType(drone.type.getType());
                break;
        }
    }
}
