package org.droidplanner.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.actionbar.ActionBarTelemFragment;
import org.droidplanner.android.fragments.widget.video.MiniWidgetSoloLinkVideo;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.view.JoystickView;
import org.droidplanner.android.view.JoystickView.JoystickListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Toby on 8/5/2015.
 */
public class ControlActivity extends DrawerNavigationUI {

    private static final float MAX_VEL = 5f, MAX_VEL_Z = 5f;

    private final static IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_ARMING);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.ATTITUDE_UPDATED:
                    updateYawParams();
                    break;

                case AttributeEvent.STATE_VEHICLE_MODE:
                case AttributeEvent.STATE_UPDATED:
                case AttributeEvent.STATE_ARMING:
                    updateVehicleReadiness();
                    break;
            }
        }
    };

    private final JoystickListener leftJoystickListener = new JoystickListener() {
        @Override
        public void joystickMoved(float x, float y) {
            float heading = computeHeading();
            sendMove();
            sendYaw(heading);
        }
    };

    private final JoystickListener rightJoystickListener = new JoystickListener() {
        @Override
        public void joystickMoved(float x, float y) {
            sendMove();
        }
    };

    private final Runnable yawUpdater = new Runnable() {
        @Override
        public void run() {
            float heading = computeHeading();
            sendYaw(heading);
        }
    };

    private final View.OnClickListener takeOffClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.getTakeOffConfirmation(dpApp.getAppPreferences(), getSupportFragmentManager(), dpApp.getDrone());
        }
    };

    private final View.OnClickListener landClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            VehicleApi.getApi(dpApp.getDrone()).setVehicleMode(VehicleMode.COPTER_LAND);
        }
    };

    private final View.OnClickListener armClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.getArmingConfirmation(getSupportFragmentManager(), dpApp.getDrone());
        }
    };

    private JoystickView leftJoystick, rightJoystick;
    private Button takeOffLand;

    private long lastReceived;
    private float lastYaw, lastYawSpeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        takeOffLand = (Button) findViewById(R.id.take_off_land_button);

        leftJoystick = (JoystickView) findViewById(R.id.left_joystick);
        leftJoystick.setSpring(JoystickView.Axis.X, true);
        leftJoystick.setSpring(JoystickView.Axis.Y, true);
        leftJoystick.setHaptic(JoystickView.Axis.Y, true);
        leftJoystick.setJoystickListener(leftJoystickListener);

        rightJoystick = (JoystickView) findViewById(R.id.right_joystick);
        rightJoystick.setSpring(JoystickView.Axis.Y, true);
        rightJoystick.setSpring(JoystickView.Axis.X, true);
        rightJoystick.setJoystickListener(rightJoystickListener);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(yawUpdater, 0, 33, TimeUnit.MILLISECONDS);

        FragmentManager fm = getSupportFragmentManager();
        Fragment miniVideoFragment = fm.findFragmentById(R.id.widget_view);
        if (!(miniVideoFragment instanceof FlightMapFragment)) {
            miniVideoFragment = new FlightMapFragment();
            fm.beginTransaction().replace(R.id.widget_view, miniVideoFragment).commit();
        }
    }

    private void toggleMapVideo(){
        FragmentManager fm = getSupportFragmentManager();
        Fragment widgetFragment = fm.findFragmentById(R.id.widget_view);
        if(widgetFragment == null || widgetFragment instanceof MiniWidgetSoloLinkVideo){
            //Default is the mapview
            widgetFragment = new FlightMapFragment();
        }
        else {
            //Assuming that the widget fragment is the map view
            widgetFragment = new MiniWidgetSoloLinkVideo();
        }
        fm.beginTransaction().replace(R.id.widget_view, widgetFragment).commit();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_control_activity, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_toggle_map_video:
                toggleMapVideo();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private float computeHeading() {
        return lastYaw + lastYawSpeed * ((System.currentTimeMillis() - lastReceived) / 1000f);
    }

    private void sendYaw(float heading) {
        float yaw = leftJoystick.getAxis(JoystickView.Axis.X);
        if (Math.abs(yaw) > JoystickView.DEADZONE) {
            Timber.d("yaw: %f", yaw);
            ControlApi.getApi(dpApp.getDrone()).turnTo((360 + (heading + yaw * 30f)) % 360, yaw, false, null);
        }
    }

    private void sendMove() {
        float throttle = leftJoystick.getAxis(JoystickView.Axis.Y);
        float x = rightJoystick.getAxis(JoystickView.Axis.X);
        float y = rightJoystick.getAxis(JoystickView.Axis.Y) * -1;

        Timber.d("x: %f, y: %f, z: %f", x, y, throttle);
        ControlApi.getApi(dpApp.getDrone()).moveAtVelocity(ControlApi.VEHICLE_COORDINATE_FRAME, y, x, throttle, null);
    }

    @Override
    protected void addToolbarFragment() {
        int toolbarId = getToolbarId();
        FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarTelem = fm.findFragmentById(toolbarId);
        if (actionBarTelem == null) {
            actionBarTelem = new ActionBarTelemFragment();
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit();
        }
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_control;
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        getBroadcastManager().unregisterReceiver(receiver);
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();
        updateYawParams();
        updateVehicleReadiness();
        getBroadcastManager().registerReceiver(receiver, eventFilter);
    }

    private void updateYawParams() {
        Drone drone = dpApp.getDrone();
        Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
        lastYaw = (float) attitude.getYaw();
        lastYawSpeed = attitude.getYawSpeed();
        lastReceived = System.currentTimeMillis();
    }

    private void updateVehicleReadiness() {
        Drone drone = dpApp.getDrone();
        State state = drone.getAttribute(AttributeType.STATE);
        boolean isFlying = state.isFlying();

        enableJoysticks(state.getVehicleMode() == VehicleMode.COPTER_GUIDED && isFlying);
        toggleTakeOffLand(state);
    }

    private void enableJoysticks(boolean enable) {
        if (leftJoystick != null) {
            leftJoystick.setEnabled(enable);
        }

        if (rightJoystick != null) {
            rightJoystick.setEnabled(enable);
        }
    }

    private void toggleTakeOffLand(State state){
        if(takeOffLand != null && state != null){
            if(state.isFlying()){
                takeOffLand.setText(R.string.label_land);
                takeOffLand.setOnClickListener(landClickListener);
            }
            else if(state.isArmed()){
                takeOffLand.setText(R.string.label_take_off);
                takeOffLand.setOnClickListener(takeOffClickListener);
            }
            else{
                takeOffLand.setText(R.string.mission_control_arm);
                takeOffLand.setOnClickListener(armClickListener);
            }
        }
    }
}
