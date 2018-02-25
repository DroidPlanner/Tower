package co.aerobotics.android.media;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import dji.common.error.DJIError;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.data.DJIFlightControllerState;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import co.aerobotics.android.graphic.map.CameraMarker;
import dji.common.camera.SystemState;
import dji.keysdk.CameraKey;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by michaelwootton on 8/29/17.
 */

public class ImageImpl {

    private List<LatLng> cameraPoints = new ArrayList<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    public static final String IMAGE_CAPTURED = "image_captured_by_drone";
    private long now = System.currentTimeMillis();
    private long lastTime = 0;
    private Double aircraftLatitude;
    private Double aircraftLongitude;
    private Float aircraftYaw;
    private FlightControllerKey aircraftLatitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LATITUDE);
    private FlightControllerKey aircraftLongitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LONGITUDE);
    private FlightControllerKey aircraftYawKey = FlightControllerKey.create(FlightControllerKey.ATTITUDE_YAW);

    private CameraKey isShootingPhotoKey = CameraKey.create(CameraKey.IS_SHOOTING_SINGLE_PHOTO);

    private KeyListener isShootingPhotoListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Boolean) {
                if ((Boolean) o1) {
                    aircraftLatitude = null;
                    aircraftLongitude = null;
                    getDroneLocation();
                }
            }
        }
    };

    public ImageImpl() {
        KeyManager.getInstance().addListener(isShootingPhotoKey, isShootingPhotoListener);
        //DroidPlannerApp.getProductInstance().getCamera().setMediaFileCallback(mediaFileCallback);
        /*
        DroidPlannerApp.getCameraInstance().setSystemStateCallback(new SystemState.Callback() {
            @Override
            public void onUpdate(@NonNull SystemState systemState) {
                if (systemState.isShootingSinglePhoto()){
                    now = System.currentTimeMillis();
                    if (now - lastTime > 1500) {
                        LatLng triggerLocation = new LatLng(DJIFlightControllerState.getInstance().getDroneLatitude(),
                                DJIFlightControllerState.getInstance().getDroneLongitude());

                        //cameraPoints.add(new LatLng(DJIFlightControllerState.getInstance().getDroneLatitude(),
                        //        DJIFlightControllerState.getInstance().getDroneLongitude()));
                        //DroidPlannerApp.getInstance().cameraMarkerInfoMap.put(triggerLocation,
                                //(float) DJIFlightControllerState.getInstance().getDroneYaw());
                        //DroidPlannerApp.getInstance().cameraPositions = cameraPoints;

                        DroidPlannerApp.getInstance().cameraMarkers.add(new CameraMarker(triggerLocation,
                                (float) DJIFlightControllerState.getInstance().getDroneYaw()));
                        notifyStatusChange();
                        lastTime = System.currentTimeMillis();
                    }
                }
            }
        }); */
    }


    private void setImagePosition() {
        if (aircraftLatitude != null && aircraftLongitude != null && aircraftYaw != null) {
            LatLng triggerLocation = new LatLng(aircraftLatitude, aircraftLongitude);
            DroidPlannerApp.getInstance().cameraMarkers.add(new CameraMarker(triggerLocation, aircraftYaw));
            notifyStatusChange();

        }
    }

    private void getDroneLocation() {
        DJISDKManager.getInstance().getKeyManager().getValue(aircraftLatitudeKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                aircraftLatitude = (Double) o;
                setImagePosition();
            }

            @Override
            public void onFailure(DJIError djiError) {
            }
        });

        DJISDKManager.getInstance().getKeyManager().getValue(aircraftLongitudeKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                aircraftLongitude = (Double) o;
                setImagePosition();
            }

            @Override
            public void onFailure(DJIError djiError) {
            }
        });

        DJISDKManager.getInstance().getKeyManager().getValue(aircraftYawKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                aircraftYaw = ((Double) o).floatValue();
                setImagePosition();
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.post(updateRunnable);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(IMAGE_CAPTURED);
            LocalBroadcastManager.getInstance(DroidPlannerApp.getInstance()).sendBroadcast(intent);
        }
    };
}
