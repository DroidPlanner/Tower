package org.droidplanner.android.glass.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;

import com.MAVLink.Messages.ApmModes;
import com.google.android.glass.view.WindowUtils;

import org.droidplanner.R;
import org.droidplanner.android.glass.fragments.GlassMapFragment;
import org.droidplanner.android.glass.utils.hud.HUD;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneEvents;
import org.droidplanner.core.drone.DroneInterfaces;

import java.util.List;

public class GlassHudActivity extends GlassUI {

    private HUD hudWidget;

    /**
     * Reference to the menu so it can be updated when used with contextual voice commands.
     */
    protected Menu mMenu;

    private GlassMapFragment mMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass_hud);

        hudWidget = (HUD) findViewById(R.id.hudWidget);
        mMapFragment = (GlassMapFragment) getSupportFragmentManager().findFragmentById(R.id
                .glass_flight_map_fragment);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.menu_glass_hud, menu);
        if(featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            mMenu = menu;
        }
        updateMenu(menu);
        return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event){
        return mMapFragment != null && mMapFragment.onGenericMotionEvent(event)
                || super.onGenericMotionEvent(event);
    }

    protected void updateMenu(Menu menu){
        if(menu != null){
            //Update the toggle connection menu title
            MenuItem connectMenuItem = menu.findItem(R.id.menu_connect);
            if (connectMenuItem != null) {
                connectMenuItem.setTitle(drone.MavClient.isConnected()
                        ? R.string.menu_disconnect
                        : R.string.menu_connect);
            }

            //Fill the flight modes menu with all the implemented flight modes
            MenuItem flightModes = menu.findItem(R.id.menu_flight_modes);
            SubMenu flightModesMenu = flightModes.getSubMenu();
            flightModesMenu.clear();

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
            if (isDroneConnected) {
                final MenuItem armingMenuItem = menu.findItem(R.id.menu_arming_state);
                if (armingMenuItem != null) {
                    boolean isArmed = drone.state.isArmed();
                    armingMenuItem.setTitle(isArmed ? R.string.menu_disarm : R.string.menu_arm);
                }
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case Menu.NONE: {

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
                return super.onMenuItemSelected(featureId, item);
        }
    }

    private void toggleArming() {
        final boolean isDroneArmed = drone.state.isArmed();
        if (!isDroneArmed) {
            app.mNotificationHandler.getTtsNotificationProvider().speak("Arming the vehicle, please standby");
        }

        MavLinkArm.sendArmMessage(drone, !isDroneArmed);
    }

    @Override
    public void onStart() {
        super.onStart();

        drone.events.addDroneListener(this);

        //Check if we're connected to the drone
        hudWidget.setEnabled(drone.MavClient.isConnected());
    }

    @Override
    public void onStop() {
        super.onStop();
        final DroneEvents droneEvents = drone.events;
        droneEvents.removeDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case ARMING:
                invalidateOptionsMenu();
                updateMenu(mMenu);

            case MODE:
                hudWidget.updateDroneState(drone.state);
                break;

            case BATTERY:
                hudWidget.updateBatteryInfo(drone.battery);
                break;

            case CONNECTED:
                invalidateOptionsMenu();
                updateMenu(mMenu);

                //Enable the hud view
                hudWidget.setEnabled(true);
                break;

            case DISCONNECTED:
                invalidateOptionsMenu();
                updateMenu(mMenu);

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
        super.onDroneEvent(event, drone);
    }
}
