package org.droidplanner.android.utils.ar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.GimbalApi;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.ar.rendering.ARSurfaceView;
import org.droidplanner.android.utils.ar.telemetry.VehicleTelemetry;

/**
 * Augmented reality (AR) activity for hosting AR view.
 *
 * TODO: This activity should probably be retained for testing AR/computer vision scenarios. The
 * FlightActivity view should show AR views in the production app.
 */
public class ARActivity extends AppCompatActivity {
    private ARSurfaceView arSurfaceView;

    private static final IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(AttributeEvent.GPS_POSITION);
        filter.addAction(AttributeEvent.HOME_UPDATED);
        filter.addAction(AttributeEvent.GIMBAL_ORIENTATION_UPDATED);
        filter.addAction(AttributeEvent.ATTITUDE_UPDATED);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Drone drone = getDrone();

            if (drone == null) {
                return;
            }

            // TODO: Is there a better way to set drone reference and start camera in renderer?
            arSurfaceView.getRenderer().setDrone(drone);

            switch (action) {
                case AttributeEvent.GIMBAL_ORIENTATION_UPDATED:
                case AttributeEvent.HOME_UPDATED:
                case AttributeEvent.GPS_POSITION:
                case AttributeEvent.ATTITUDE_UPDATED:
                    addTelemetryDataToSyncer(drone);
                    break;
            }
        }
    };

    private Drone getDrone(){
        return ((DroidPlannerApp) getApplication()).getDrone();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize and set GLSurfaceView as view for this activity.
        arSurfaceView = new ARSurfaceView(this);
        setContentView(arSurfaceView);
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);

        ARWorld.getInstance().getTelemetryToVideoSyncer().clearBufferedTelemetryData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        arSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        arSurfaceView.onPause();
    }

    private void addTelemetryDataToSyncer(Drone drone) {
        // Get vehicle and home location and gimbal orientation.
        Gps gps = drone.getAttribute(AttributeType.GPS);
        Home home = drone.getAttribute(AttributeType.HOME);

        GimbalApi.GimbalOrientation gimbalOrientation = GimbalApi.getApi(getDrone()).getGimbalOrientation();

        // TODO: Investigate why gimbal yaw is returning 0.0.
        Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);

        LatLongAlt vehicleLocation = new LatLongAlt(0.0, 0.0, 0.0);
        if (gps.isValid()) {
            vehicleLocation = new LatLongAlt(gps.getPosition().getLatitude(), gps.getPosition().getLongitude(), 0.0);
        }

        LatLongAlt homeLocation = new LatLongAlt(0.0, 0.0, 0.0);
        if (home.isValid() && home.getCoordinate() != null) {
            homeLocation = new LatLongAlt(home.getCoordinate().getLatitude(), home.getCoordinate().getLongitude(),
                home.getCoordinate().getAltitude());
        }

        // Update telemetry-to-video syncer and ARWorld with telemetry data.
        // TODO: Reduce redundancy in telemetry updates.
        VehicleTelemetry vehicleTelemetry =
            new VehicleTelemetry((float) attitude.getYaw(), gimbalOrientation.getPitch(),
                vehicleLocation, homeLocation, System.currentTimeMillis());

        ARWorld.getInstance().getTelemetryToVideoSyncer().bufferTelemetryData(vehicleTelemetry);

        ARWorld.getInstance().setVehicleLocation(vehicleLocation);
        ARWorld.getInstance().setHomeLocation(homeLocation);
    }
}