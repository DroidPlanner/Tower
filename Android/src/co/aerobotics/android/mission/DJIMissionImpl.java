package co.aerobotics.android.mission;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.data.DJIFlightControllerState;
import co.aerobotics.android.media.ImageImpl;
import co.aerobotics.android.proxy.mission.MissionProxy;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.WhiteBalance;
import dji.common.error.DJIError;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.mission.MissionState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.ActionCallback;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by michaelwootton on 8/21/17.
 */

public class DJIMissionImpl implements WaypointMissionOperatorListener{

    private static final String TAG = "dji_mission_impl";
    public static final String MISSION_START = "on_mission_start";
    public static final String MiSSION_STOP = "on_mission_stop";
    public static final String ERROR_SD_CARD = "sd_card_error";
    public static final String ERROR_CAMERA= "camera_trigger_speed_error";
    public static final String ERROR_MISSION_START = "mission_start_error";
    public static final String UPLOAD_STARTING = "upload_starting";

    private WaypointMissionOperator instance;
    public boolean cameraStarted = false;
    private Intent intent;
    public ImageImpl imageImpl;
    private SurveyDetail surveyDetail;
    private FlightControllerKey goHomeKey = FlightControllerKey.create(FlightControllerKey.START_GO_HOME);

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public DJIMissionImpl(){
        //missionProxy = DroidPlannerApp.getInstance().getMissionProxy();
    }

    /*
    Runnables
     */

    private Runnable notifyStatus = new Runnable() {
        @Override
        public void run() {
            LocalBroadcastManager.getInstance(DroidPlannerApp.getInstance()).sendBroadcast(intent);
        }
    };

    /*
    Mission Operations
     */

    public void run(MissionProxy missionProxy){
        if (missionProxy.getItems().isEmpty()){
            return;
        }
        surveyDetail= ((Survey) missionProxy.getItems().get(0).getMissionItem()).getSurveyDetail();
        if(!isValidTriggerSpeed()){
            intent = new Intent(ERROR_CAMERA);
            mainHandler.post(notifyStatus);
            return;
        }

        if (surveyDetail.getCameraDetail().getName().contains("Agri")) {
            turnOffObstacleAvoidance();
        }

        List<LatLong> points = missionProxy.getPathPoints();
        if (!points.isEmpty()) {
            WaypointMission mission = buildMission(points);
            if(mission != null){
                loadMission(mission);
            }
            setMissionListener();
            uploadMission();
            setCameraMode();
            setCameraWhiteBalance(surveyDetail);
            setAspectRatio();
        }
    }

    public WaypointMission buildMission(List<LatLong> points){
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder();
        List<Waypoint> waypointList = new ArrayList<>();

        for (LatLong point : points) {
            LatLng pointLatLng = new LatLng(point.getLatitude(), point.getLongitude());
            Waypoint mWaypoint = new Waypoint(pointLatLng.latitude, pointLatLng.longitude, getFlightAltitude());
            waypointList.add(mWaypoint);
        }
        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        waypointMissionBuilder.finishedAction(WaypointMissionFinishedAction.GO_HOME)
                .flightPathMode(WaypointMissionFlightPathMode.CURVED)
                .headingMode(WaypointMissionHeadingMode.AUTO)
                .autoFlightSpeed(getFlightSpeed())
                .maxFlightSpeed(getFlightSpeed());

        //shootPhotoTimeInterval not supported on older firmware versions
        if (DroidPlannerApp.isFirmwareNewVersion() != null && DroidPlannerApp.isFirmwareNewVersion()){
            waypointMissionBuilder.setGimbalPitchRotationEnabled(true);
            if (waypointMissionBuilder.getWaypointList().size() > 0) {
                for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                    //waypointMissionBuilder.getWaypointList().get(i).shootPhotoTimeInterval = getCameraTriggerSpeed();
                    waypointMissionBuilder.getWaypointList().get(i).shootPhotoDistanceInterval
                            = (float) getSurveyDetail().getLongitudinalPictureDistance();
                    waypointMissionBuilder.getWaypointList().get(i).cornerRadiusInMeters = 0.3f;
                    waypointMissionBuilder.getWaypointList().get(i).gimbalPitch = -90f;
                }
            }
        } else {
            initCamera((int) getCameraTriggerSpeed());
        }

        DJIError check = waypointMissionBuilder.checkParameters();
        if (check == null) {
            return waypointMissionBuilder.build();
        } else {
            return null;
        }
    }

    //Loads mission object into device memory
    private boolean loadMission(WaypointMission mission){
        DJIError error = getWaypointMissionOperator().loadMission(mission);
        return error == null;
    }

    public void setMissionListener(){
        getWaypointMissionOperator().removeListener(this);
        getWaypointMissionOperator().addListener(this);
    }

    //Upload mission to drone
    private void uploadMission() {
        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                } else {
                    getWaypointMissionOperator().retryUploadMission((new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError error) {
                            if (error == null) {
                                Log.d(TAG, "Upload Success");
                            } else {
                                Log.e(TAG, "Upload Failed");

                            }
                        }
                    }));
                }
            }
        });
    }

    private void startWaypointMission() {
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(final DJIError error) {
                if (error == null) {
                    imageImpl = new ImageImpl();
                    rotateGimbal(-90.0f, 0.1);
                    intent = new Intent(MISSION_START);
                    mainHandler.post(notifyStatus);
                } else {
                    String errorMsg = error.getDescription();
                    if (!errorMsg.equals("The execution could not be executed")) {
                        intent = new Intent((ERROR_MISSION_START)).putExtra("errorMessage", error.getDescription());
                        mainHandler.post(notifyStatus);
                    }

                }
            }
        });
    }

    //Called when user wants to manually abort mission
    public void stopWaypointMission() {
        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null){
                    goHome();
                    intent = new Intent(MiSSION_STOP);
                    mainHandler.post(notifyStatus);
                    getWaypointMissionOperator().removeListener(DJIMissionImpl.this);

                }
            }
        });
    }

    public void goHome() {
        /**
         * Command aircraft to return to home position
         */
        KeyManager.getInstance().performAction(goHomeKey, new ActionCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });
    }

    /*
    Getter Methods
     */

    private synchronized WaypointMissionOperator getWaypointMissionOperator() {
        if(instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return instance;
    }
    private SurveyDetail getSurveyDetail(){
        //MissionItem firstItem = missionProxy.getItems().get(0).getMissionItem();
        //return ((Survey) firstItem).getSurveyDetail();
        return surveyDetail;
    }

    public float getCameraTriggerSpeed(){
        double cameraDistance = getSurveyDetail().getLongitudinalPictureDistance();
        return (float) (cameraDistance / getFlightSpeed());
    }

    private float getFlightAltitude(){
        return (float) getSurveyDetail().getAltitude();
    }

    private float getFlightSpeed() {
        return (float) getSurveyDetail().getSpeed();
    }

    /*
    Check Methods
    */

    private boolean isValidTriggerSpeed(){
        return getCameraTriggerSpeed() >=2f;
    }

    private boolean isSdCardInserted(){
        return DroidPlannerApp.getInstance().isSDCardInserted;
    }

    /*
    Drone Control Methods
     */
    void initCamera(int photoTime){
        final Camera camera = DroidPlannerApp.getCameraInstance();
        if(camera != null){
            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.INTERVAL;
            SettingsDefinitions.PhotoTimeIntervalSettings photoTimeIntervalSettings = new SettingsDefinitions.PhotoTimeIntervalSettings(255, photoTime);
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        Log.e(TAG, djiError.getDescription());
                    }
                }
            });

            camera.setPhotoTimeIntervalSettings(photoTimeIntervalSettings, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        Log.e(TAG, djiError.getDescription());
                    }
                }
            });
        }
    }

    public void setCameraMode() {
        /**
         * Set the camera mode to shoot photo
         */
        final Camera camera = DroidPlannerApp.getCameraInstance();
        if (camera != null) {
            camera.getMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.CameraMode>() {
                @Override
                public void onSuccess(SettingsDefinitions.CameraMode cameraMode) {
                    if (cameraMode != SettingsDefinitions.CameraMode.SHOOT_PHOTO) {
                        SettingsDefinitions.CameraMode photoMode = SettingsDefinitions.CameraMode.SHOOT_PHOTO;
                        camera.setMode(photoMode, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {

                            }
                        });
                    }
                }

                @Override
                public void onFailure(DJIError djiError) {

                }
            });
        }
    }

    void setCameraWhiteBalance(SurveyDetail surveyDetail){
        final Camera camera = DroidPlannerApp.getCameraInstance();
        if(camera != null){
            WhiteBalance whiteBalance;
            if(surveyDetail.isSunny()) {
                SettingsDefinitions.WhiteBalancePreset whiteBalancePreset = SettingsDefinitions.WhiteBalancePreset.SUNNY;
                whiteBalance = new WhiteBalance(whiteBalancePreset);
            } else {
                SettingsDefinitions.WhiteBalancePreset whiteBalancePreset = SettingsDefinitions.WhiteBalancePreset.CLOUDY;
                whiteBalance = new WhiteBalance(whiteBalancePreset);
            }
            if(whiteBalance != null) {
                camera.setWhiteBalance(whiteBalance, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                    }
                });
            }
        }
    }

    void setAspectRatio() {
        final Camera camera = DroidPlannerApp.getCameraInstance();
        if(camera!=null){
            camera.setPhotoAspectRatio(SettingsDefinitions.PhotoAspectRatio.RATIO_4_3, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
        }
    }

    void startCamera(){
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Camera camera = DroidPlannerApp.getCameraInstance();
                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            cameraStarted = true;
                        } else {
                            //setResultToToast("start camera error: " + djiError.getDescription());
                            cameraStarted = false;
                        }
                    }
                });
            }
        }, 100);
    }

    void stopCamera(){
        final Camera camera = DroidPlannerApp.getCameraInstance();
        if (cameraStarted) {
            if (camera != null && !DroidPlannerApp.isFirmwareNewVersion()) {
                camera.stopShootPhoto(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null) {
                            Log.e(TAG, "Camera Stopped");
                            cameraStarted = false;
                        } else {
                            Log.e(TAG, "stop camera error: " + djiError.getDescription());
                        }
                    }
                });
            }
        }
    }

    public void rotateGimbal(float pitchAngle, double rotateTime){
        Gimbal gimbal = DroidPlannerApp.getAircraftInstance().getGimbal();
        Rotation.Builder rotationBuilder = new Rotation.Builder();
        rotationBuilder.mode(RotationMode.ABSOLUTE_ANGLE);
        rotationBuilder.pitch(pitchAngle);
        rotationBuilder.time(rotateTime);

        Rotation rotation = rotationBuilder.build();

        gimbal.rotate(rotation, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null){
                    Log.i(TAG, "Successfully rotated gimbal");
                }
            }
        });

    }


    @Override
    public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

    }

    @Override
    public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {
        if (waypointMissionUploadEvent.getCurrentState().equals(WaypointMissionState.READY_TO_EXECUTE)) {
            startWaypointMission();
            rotateGimbal(-90.0f, 0.1);
        }

        if (waypointMissionUploadEvent.getProgress() != null) {
            intent = new Intent(UPLOAD_STARTING);
            intent.putExtra("TOTAL_WAYPOINTS", String.valueOf(waypointMissionUploadEvent.getProgress().totalWaypointCount));
            intent.putExtra("WAYPOINT", String.valueOf(waypointMissionUploadEvent.getProgress().uploadedWaypointIndex + 1));
            mainHandler.post(notifyStatus);
        }
    }

    @Override
    public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
        if(waypointMissionExecutionEvent.getProgress() != null && waypointMissionExecutionEvent.getProgress().targetWaypointIndex == 1){
            if (DroidPlannerApp.isFirmwareNewVersion() == null || !DroidPlannerApp.isFirmwareNewVersion()) {
                if(!cameraStarted) {
                    cameraStarted = true;
                    startCamera();
                }
            }
        }
    }

    @Override
    public void onExecutionStart() {
        rotateGimbal(-90.0f, 0.1);
    }

    @Override
    public void onExecutionFinish(@Nullable DJIError djiError) {
        Log.d(TAG, "onFinish");
        rotateGimbal(0, 0.5);
        stopCamera();
        intent = new Intent(MiSSION_STOP);
        mainHandler.post(notifyStatus);
        getWaypointMissionOperator().removeListener(DJIMissionImpl.this);
    }

    public void turnOffObstacleAvoidance() {
        FlightAssistant flightAssistant = ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController().getFlightAssistant();
        if (flightAssistant != null) {
            flightAssistant.setCollisionAvoidanceEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            flightAssistant.setUpwardsAvoidanceEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            flightAssistant.setActiveObstacleAvoidanceEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            flightAssistant.setPrecisionLandingEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            flightAssistant.setVisionAssistedPositioningEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            flightAssistant.setPrecisionLandingEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
            flightAssistant.setLandingProtectionEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                }
            });
        }
    }
}
