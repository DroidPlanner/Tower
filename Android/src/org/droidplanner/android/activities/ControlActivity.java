package org.droidplanner.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.actionbar.ActionBarTelemFragment;
import org.droidplanner.android.fragments.widget.MiniWidgetSoloLinkVideo;
import org.droidplanner.android.view.JoystickView;

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
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.ATTITUDE_UPDATED:
                    updateYawParams();
                    break;

                case AttributeEvent.STATE_VEHICLE_MODE:
                    State state = dpApp.getDrone().getAttribute(AttributeType.STATE);
                    mode = state.getVehicleMode();
                    break;
            }
        }
    };

    private JoystickView leftJoystick, rightJoystick;

    private long lastReceived;
    private float lastYaw, lastYawSpeed;
    private VehicleMode mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        final Bitmap reticle = BitmapFactory.decodeResource(getResources(), R.drawable.ic_control_grey_600_24dp);

        leftJoystick = (JoystickView) findViewById(R.id.left_joystick);
        leftJoystick.setReticle(reticle);
        leftJoystick.setSpring(JoystickView.Axis.X, true);
        leftJoystick.setSpring(JoystickView.Axis.Y, true);
        leftJoystick.setHaptic(JoystickView.Axis.Y, true);
        leftJoystick.setJoystickListener(new JoystickView.JoystickListener() {
            @Override
            public void joystickMoved(float x, float y) {
                float heading = computeHeading();
                sendMove(heading);
                sendYaw(heading);
            }
        });

        rightJoystick = (JoystickView) findViewById(R.id.right_joystick);
        rightJoystick.setReticle(reticle);
        rightJoystick.setSpring(JoystickView.Axis.Y, true);
        rightJoystick.setSpring(JoystickView.Axis.X, true);
        rightJoystick.setJoystickListener(new JoystickView.JoystickListener() {
            @Override
            public void joystickMoved(float x, float y) {
                float heading = computeHeading();
                sendMove(heading);
            }
        });

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                float heading = computeHeading();
                sendYaw(heading);
            }
        }, 0, 33, TimeUnit.MILLISECONDS);

        FragmentManager fm = getSupportFragmentManager();
        Fragment miniVideoFragment = fm.findFragmentById(R.id.widget_view);
        if (!(miniVideoFragment instanceof MiniWidgetSoloLinkVideo)) {
            miniVideoFragment = new MiniWidgetSoloLinkVideo();
            fm.beginTransaction().replace(R.id.widget_view, miniVideoFragment).commit();
        }
    }

    private float computeHeading() {
        return lastYaw + lastYawSpeed * ((System.currentTimeMillis() - lastReceived) / 1000f);
    }

    private void sendYaw(float heading) {
        if (mode == null || !mode.equals(VehicleMode.COPTER_GUIDED))
            return;

        float yaw = leftJoystick.getAxis(JoystickView.Axis.X);
        heading /= Math.PI;
        heading *= 180f;
        if (Math.abs(yaw) > 0.05) {
            ControlApi.getApi(dpApp.getDrone()).turnTo((360 + (heading + yaw * 30f)) % 360,
                    Math.abs(yaw) * 30f, Float.compare(Math.signum(yaw), 1f) == 0, false, null);
        }
    }

    private void sendMove(float heading) {
        float throttle = leftJoystick.getAxis(JoystickView.Axis.Y);
        float x = rightJoystick.getAxis(JoystickView.Axis.X);
        float y = rightJoystick.getAxis(JoystickView.Axis.Y);
        float yaw = leftJoystick.getAxis(JoystickView.Axis.X);
        if (mode != null && mode.equals(VehicleMode.COPTER_GUIDED)) {
            if (x != 0 && y != 0) {
                double theta = Math.atan(y / x);
                if (theta < 0) {
                    theta += Math.PI;
                }
                if (y < 0) {
                    theta += Math.PI;
                }
                theta += Math.PI / 2;
                double magnitude = Math.sqrt(x * x + y * y);
                x = (float) (Math.cos(heading + theta) * magnitude);
                y = (float) (Math.sin(heading + theta) * magnitude);

            }
        } else {
            y = -y;
            throttle = -throttle;
        }
        Timber.d("x: %f, y: %f, z: %f, yaw: %f", x, y, throttle, yaw);
        ControlApi.getApi(dpApp.getDrone()).setVelocity(x * MAX_VEL, y * MAX_VEL, throttle * MAX_VEL_Z, null);
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
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
    protected int getNavigationDrawerEntryId() {
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
        getBroadcastManager().registerReceiver(receiver, eventFilter);
    }

    private void updateYawParams() {
        final Drone drone = dpApp.getDrone();
        final Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
        lastYaw = (float) attitude.getYaw();
        lastYawSpeed = attitude.getYawSpeed();
        lastReceived = System.currentTimeMillis();
    }
}
