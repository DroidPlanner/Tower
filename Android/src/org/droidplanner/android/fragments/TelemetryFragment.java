package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.android.client.apis.solo.SoloCameraApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.WidgetActivity;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;
import org.droidplanner.android.widgets.AttitudeIndicator;

import timber.log.Timber;

public class TelemetryFragment extends ApiListenerFragment {

    private static final String TAG = TelemetryFragment.class.getSimpleName();

    private final static IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
        eventFilter.addAction(AttributeEvent.SPEED_UPDATED);
		        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(AttributeEvent.HOME_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case AttributeEvent.ATTITUDE_UPDATED:
                    onOrientationUpdate();
                    break;

                case AttributeEvent.SPEED_UPDATED:
                    onSpeedUpdate();
                    break;
                case AttributeEvent.GPS_POSITION:
                case AttributeEvent.HOME_UPDATED:
                    onPositionUpdate();
                    break;					

                case AttributeEvent.STATE_CONNECTED:
                    tryStreamingVideo();
                    break;
            }
        }
    };

    private AttitudeIndicator attitudeIndicator;
    private TextView roll;
    private TextView yaw;
    private TextView pitch;

    private TextView horizontalSpeed;
    private TextView verticalSpeed;
	
	    private TextView latitude;
    private TextView longitude;

    private View videoContainer;
    private TextureView videoView;

    private boolean headingModeFPV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_telemetry, container, false);
        attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

        roll = (TextView) view.findViewById(R.id.rollValueText);
        yaw = (TextView) view.findViewById(R.id.yawValueText);
        pitch = (TextView) view.findViewById(R.id.pitchValueText);

        horizontalSpeed = (TextView) view.findViewById(R.id.horizontal_speed_telem);
        verticalSpeed = (TextView) view.findViewById(R.id.vertical_speed_telem);
		
		        latitude = (TextView) view.findViewById(R.id.latitude_telem);
        longitude = (TextView) view.findViewById(R.id.longitude_telem);

        videoContainer = view.findViewById(R.id.minimized_video_container);
        videoContainer.setVisibility(View.GONE);
        videoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), WidgetActivity.class)
                        .putExtra(WidgetActivity.EXTRA_WIDGET_ID, WidgetActivity.WIDGET_SOLOLINK_VIDEO));
            }
        });

        videoView = (TextureView) view.findViewById(R.id.minimized_video);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
    }

    @Override
    public void onApiConnected() {
        updateAllTelem();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onResume(){
        super.onResume();
        tryStreamingVideo();
    }

    @Override
    public void onPause(){
        super.onPause();
        tryStoppingVideoStream();
    }

    @Override
    public void onApiDisconnected() {
        tryStoppingVideoStream();
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    private void updateAllTelem() {
        onOrientationUpdate();
        onSpeedUpdate();
        tryStreamingVideo();
    }

    private void tryStoppingVideoStream() {
        final Drone drone = getDrone();
        Timber.d("Stopping video stream with tag %s.", TAG);
        SoloCameraApi.getApi(drone).stopVideoStream(TAG, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                Timber.d("Video streaming stopped successfully.");
            }

            @Override
            public void onError(int i) {
                Timber.d("Unable to stop video streaming: %d", i);
            }

            @Override
            public void onTimeout() {
                Timber.d("Timed out while trying to stop video streaming.");
            }
        });
    }

    private void tryStreamingVideo() {
        if(!isAttached())
            return;

        final Drone drone = getDrone();
        CapabilityApi.getApi(drone).checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING, new CapabilityApi.FeatureSupportListener() {
            @Override
            public void onFeatureSupportResult(String featureId, int result, Bundle bundle) {
                switch (result) {
                    case CapabilityApi.FEATURE_SUPPORTED:
                        if (videoContainer != null) {
                            videoContainer.setVisibility(View.VISIBLE);
                            videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                                @Override
                                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                                    Timber.d("Starting video with tag %s", TAG);
                                    SoloCameraApi.getApi(drone).startVideoStream(new Surface(surface), TAG, new SimpleCommandListener() {

                                        @Override
                                        public void onSuccess() {
                                            Timber.d("Video started successfully.");
                                        }

                                        @Override
                                        public void onError(int i) {
                                            Timber.d("Starting video error: %d", i);
                                        }

                                        @Override
                                        public void onTimeout() {
                                            Timber.d("Starting video timeout.");
                                        }
                                    });
                                }

                                @Override
                                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                                }

                                @Override
                                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                                    tryStoppingVideoStream();
                                    return true;
                                }

                                @Override
                                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                                }
                            });
                        }
                        break;

                    default:
                        if (videoContainer != null) {
                            videoContainer.setVisibility(View.GONE);
                        }
                }
            }
        });
    }

    private void onOrientationUpdate() {
        if(!isAttached())
            return;

        final Drone drone = getDrone();

        final Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
        if (attitude == null)
            return;

        float r = (float) attitude.getRoll();
        float p = (float) attitude.getPitch();
        float y = (float) attitude.getYaw();

        if (!headingModeFPV & y < 0) {
            y = 360 + y;
        }

        attitudeIndicator.setAttitude(r, p, y);

        roll.setText(String.format("%3.0f\u00B0", r));
        pitch.setText(String.format("%3.0f\u00B0", p));
        yaw.setText(String.format("%3.0f\u00B0", y));

    }

    private void onSpeedUpdate() {
        if(!isAttached())
            return;

        final Drone drone = getDrone();
        final Speed speed = drone.getAttribute(AttributeType.SPEED);

        final double groundSpeedValue = speed != null ? speed.getGroundSpeed() : 0;
        final double verticalSpeedValue = speed != null ? speed.getVerticalSpeed() : 0;

        final SpeedUnitProvider speedUnitProvider = getSpeedUnitProvider();

        horizontalSpeed.setText(getString(R.string.horizontal_speed_telem, speedUnitProvider.boxBaseValueToTarget(groundSpeedValue).toString()));
        verticalSpeed.setText(getString(R.string.vertical_speed_telem, speedUnitProvider.boxBaseValueToTarget(verticalSpeedValue).toString()));
    }
	
	    private void onPositionUpdate() {
        if(!isAttached())
            return;

        final Drone drone = getDrone();
        final Gps droneGps = drone.getAttribute(AttributeType.GPS);

        if (droneGps != null && droneGps.isValid()) {

            final double latitudeValue = droneGps.getPosition().getLatitude();
            final double longitudeValue = droneGps.getPosition().getLongitude();

            latitude.setText(getString(R.string.latitude_telem, Location.convert(latitudeValue, Location.FORMAT_DEGREES)));
            longitude.setText(getString(R.string.longitude_telem, Location.convert(longitudeValue, Location.FORMAT_DEGREES)));

        }


    }

}
