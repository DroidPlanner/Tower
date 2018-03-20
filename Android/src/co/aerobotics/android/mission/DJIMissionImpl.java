package co.aerobotics.android.mission;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.data.SQLiteDatabaseHandler;
import co.aerobotics.android.media.ImageImpl;
import co.aerobotics.android.proxy.mission.MissionProxy;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.WhiteBalance;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControlState;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.GoHomeAssessment;
import dji.common.flightcontroller.SmartRTHState;
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
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.Mission;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by michaelwootton on 8/21/17.
 */

public class DJIMissionImpl {

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
    private Context context;
    private SharedPreferences sharedPreferences;
    private boolean isTimelineMission = false;
    private SmartRTHState rthState;
    private MissionControl.Listener timelineListener = new MissionControl.Listener() {
        @Override
        public void onEvent(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {
            updateTimelineStatus(element, event, error);
        }
    };

    private WaypointMissionOperatorListener waypointMissionOperatorListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent waypointMissionUploadEvent) {
            if (waypointMissionUploadEvent.getCurrentState().equals(WaypointMissionState.READY_TO_EXECUTE)) {
                rotateGimbal(-90.0f, 0.1);
                if (!isTimelineMission) {
                    startWaypointMission();
                }
            }

            if (waypointMissionUploadEvent.getProgress() != null) {
                intent = new Intent(UPLOAD_STARTING);
                intent.putExtra("TOTAL_WAYPOINTS", String.valueOf(waypointMissionUploadEvent.getProgress().totalWaypointCount));
                intent.putExtra("WAYPOINT", String.valueOf(waypointMissionUploadEvent.getProgress().uploadedWaypointIndex + 1));
                mainHandler.post(notifyStatus);
            }
        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
            if (waypointMissionExecutionEvent.getProgress() != null && waypointMissionExecutionEvent.getProgress().targetWaypointIndex == 1) {
                if (!cameraStarted && !DroidPlannerApp.isFirmwareNewVersion()) {
                    cameraStarted = true;
                    startCamera();
                }
            }

            if (waypointMissionExecutionEvent.getProgress() != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(context.getString(R.string.last_waypoint_index), waypointMissionExecutionEvent.getProgress().targetWaypointIndex);
                editor.apply();
            }
        }

        @Override
        public void onExecutionStart() {
            SharedPreferences.Editor editor;

            rotateGimbal(-90.0f, 0.1);
            if (isTimelineMission) {
                editor = sharedPreferences.edit();
                editor.putInt(context.getString(R.string.survey_index), MissionControl.getInstance().getCurrentTimelineMarker());
                editor.apply();
                if (MissionControl.getInstance().getCurrentTimelineMarker() == 0) {
                    intent = new Intent(DJIMissionImpl.MISSION_START);
                    mainHandler.post(notifyStatus);
                    editor.putBoolean(context.getString(R.string.mission_aborted), false);
                    editor.apply();
                }
            } else {
                editor = sharedPreferences.edit();
                editor.putInt(context.getString(R.string.survey_index), 0);
                editor.putBoolean(context.getString(R.string.mission_aborted), false);
                editor.apply();
                intent = new Intent(DJIMissionImpl.MISSION_START);
                mainHandler.post(notifyStatus);
            }
        }

        @Override
        public void onExecutionFinish(DJIError djiError) {
            Log.d(TAG, "onFinish");
            if (!isTimelineMission) {
                rotateGimbal(0, 0.5);
                stopCamera();
                intent = new Intent(MiSSION_STOP);
                mainHandler.post(notifyStatus);
                getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
            }
            // check if mission was ended due to smart return to launch
            if (rthState != null && rthState.equals(SmartRTHState.EXECUTED)) {
                SharedPreferences.Editor editor;
                editor = sharedPreferences.edit();
                editor.putBoolean(context.getString(R.string.mission_aborted), true);
                editor.apply();
            }
        }
    };

    public DJIMissionImpl(){
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

    public void initializeMission(MissionProxy missionProxy, Context context, boolean resume) {
        if (DroidPlannerApp.isFirmwareNewVersion() == null) {
            DroidPlannerApp.getInstance().getFirmwareVersion();
        }
        FlightController flightController = ((Aircraft) DroidPlannerApp.getProductInstance()).getFlightController();
        flightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(FlightControllerState flightControllerState) {
                rthState = flightControllerState.getGoHomeAssessment().getSmartRTHState();
                Log.i(TAG, rthState.toString());
            }
        });

        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        List<MissionDetails> missionsToSurvey;
        if (resume) {
            missionsToSurvey = getMissionDetailsFromDb(context, sharedPreferences);
        } else {
            missionsToSurvey = getMissionDetailsFromMissionProxyItems(missionProxy);
            //run checks
            if (missionsToSurvey == null || missionsToSurvey.isEmpty()) {
                return;
            }
            //isValidMission();
            setCameraParameters(missionProxy);
        }

        //delete missions from db
        deletePreviousMissions(context);
        //save the new missions to db
        saveMissionDetailsToDb(context, missionsToSurvey);
        //set waypoint mission listener
        setMissionListener();
        //build waypoint missions
        List<WaypointMission> waypointMissions = getWaypointMissionList(missionsToSurvey);
        //if more than one mission then setup timeline mission
        if (waypointMissions.size() > 0) {
            isTimelineMission = true;
            //Schedule timeline elements in mission control
            List<TimelineElement> elements = getTimelineElements(waypointMissions);
            MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
            if (missionControl.scheduledCount() > 0) {
                missionControl.unscheduleEverything();
                missionControl.removeAllListeners();
            }
            missionControl.scheduleElements(elements);
            //start timeline timelineListener
            missionControl.addListener(timelineListener);
            //start timeline mission
            startTimelineMission();
        } else {
            WaypointMission mission = waypointMissions.get(0);
            if(mission != null){
                loadMission(mission);
            }
            uploadMission();
        }
        turnOffObstacleAvoidance();
    }

    private void setCameraParameters(MissionProxy missionProxy) {
        SurveyDetail surveyDetail = ((Survey) missionProxy.getItems().get(0).getMissionItem()).getSurveyDetail();
        setCameraWhiteBalance(surveyDetail);
        setAspectRatio();
        setCameraMode();
    }

    private boolean isValidMission() {
        if(true){
            intent = new Intent(ERROR_CAMERA);
            mainHandler.post(notifyStatus);
            return false;
        }
        return true;
    }

    private List<TimelineElement> getTimelineElements(List<WaypointMission> waypointMissions) {
        List<TimelineElement> elements = new ArrayList<>();
        for (WaypointMission mission : waypointMissions) {
            elements.add(Mission.elementFromWaypointMission(mission));
        }
        return elements;
    }

    private void saveMissionDetailsToDb(Context context, List<MissionDetails> missionDetailsList) {
        new SQLiteDatabaseHandler(context).addMissionDetails(missionDetailsList);
    }

    private List<MissionDetails> getMissionDetailsFromMissionProxyItems(MissionProxy missionProxy) {
        /**
         * Return list of MissionDetails objects from mission proxy
         */
        List<MissionItemProxy> items = missionProxy.getItems();
        List<MissionDetails> missionsToSurvey = new ArrayList<>();
        for (MissionItemProxy itemProxy : items) {
            MissionItem item = itemProxy.getMissionItem();
            if (item instanceof Survey) {
                List<LatLong> points = ((Survey) item).getGridPoints();
                float altitude = (float) ((Survey) item).getSurveyDetail().getAltitude();
                float speed = (float) ((Survey) item).getSurveyDetail().getSpeed();
                float imageDistance = (float) ((Survey) item).getSurveyDetail().getLongitudinalPictureDistance();
                if (isValidTriggerSpeed(imageDistance, speed)) {
                    MissionDetails missionDetails = getCurrentMissionDetails(points, speed, imageDistance, altitude);
                    missionsToSurvey.add(missionDetails);
                } else {
                    return null;
                }

            }
        }
        return missionsToSurvey;
    }

    private List<MissionDetails> getMissionDetailsFromDb(Context context, SharedPreferences sharedPreferences) {
        /**
         * Return list of MissionDetails objects from local db
         */
        int startWaypointIndex = sharedPreferences.getInt(context.getString(R.string.last_waypoint_index), -1);
        List<MissionDetails> missionDetailsList = getPreviousMissionDetails(context);
        List<MissionDetails> missionsToSurvey = missionDetailsList.subList(sharedPreferences.getInt(context.getString(R.string.survey_index), -1), missionDetailsList.size());
        List<LatLong> newFirstBoundaryWaypoints = getWaypointsFromString(missionsToSurvey.get(0).getWaypoints(), startWaypointIndex);
        missionsToSurvey.get(0).setWaypoints(convertWaypointToString(newFirstBoundaryWaypoints));
        return missionsToSurvey;
    }

    private List<WaypointMission> getWaypointMissionList(List<MissionDetails> missionsToSurvey) {
        /**
         * Return list of WaypointMission objects
         */
        List<WaypointMission> waypointMissions = new ArrayList<>();
        for (int i = 0; i < missionsToSurvey.size(); i++) {
            if (i == missionsToSurvey.size() - 1) {
                WaypointMission mission = buildMission(missionsToSurvey.get(i),
                        getWaypointsFromString(missionsToSurvey.get(i).getWaypoints(), 0),
                        WaypointMissionFinishedAction.GO_HOME);
                if (mission != null) {
                    waypointMissions.add(mission);
                }
            } else {
                WaypointMission mission = buildMission(missionsToSurvey.get(i),
                        getWaypointsFromString(missionsToSurvey.get(i).getWaypoints(), 0),
                        WaypointMissionFinishedAction.NO_ACTION);
                if (mission != null) {
                    waypointMissions.add(mission);
                }
            }
        }
        return waypointMissions;
    }

    private void updateTimelineStatus(TimelineElement element, TimelineEvent event, final DJIError error) {

        switch (event) {
            case STARTED:
                // null element refers to global event
                if (element == null) {
                    imageImpl = new ImageImpl();
                    rotateGimbal(-90, 0.1);
                }
                break;
            case STOPPED:
            case FINISHED:
                if (element == null) {
                    intent = new Intent(DJIMissionImpl.MiSSION_STOP);
                    mainHandler.post(notifyStatus);
                    rotateGimbal(0, 0.5);
                    if (!DroidPlannerApp.isFirmwareNewVersion()) {
                        stopCamera();
                    }
                    getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
                }
                break;
            case START_ERROR:
                break;
            default:
                break;
        }
    }
/*
    public void run(MissionProxy missionProxy, Context context, Boolean resume){
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(context.getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        if (resume) {
            //get previous mission details from db
            MissionDetails missionDetails = getPreviousMissionDetails(context).get(0);
            if (missionDetails == null) {
               return;
            }
            //delete mission from db
            deletePreviousMissions(context);
            //convert string waypoints to List<latlong>
            List<LatLong> points = getWaypointsFromString(missionDetails.getWaypoints(),
                    sharedPreferences.getInt(context.getString(R.string.last_waypoint_index), -1));
            //save the new mission with updated waypoints to db
            saveMissionDetails(context, missionDetails.getAltitude(), missionDetails.getImageDistance(),
                    convertWaypointToString(points), missionDetails.getSpeed());
            //build mission
            WaypointMission mission = buildMission(missionDetails, points, WaypointMissionFinishedAction.GO_HOME);
            if(mission != null){
                loadMission(mission);
            }

        } else {
            surveyDetail= ((Survey) missionProxy.getItems().get(0).getMissionItem()).getSurveyDetail();
            //get current mission details
            MissionDetails missionDetails = getCurrentMissionDetails(missionProxy.getPathPoints(), getFlightSpeed(), getImageDistance(), getFlightAltitude());
            //delete previous mission
            deletePreviousMissions(context);
            //save new mission to db
            saveMissionDetails(context, missionDetails.getAltitude(), missionDetails.getImageDistance(),
                    missionDetails.getWaypoints(), missionDetails.getSpeed());
            //build mission
            WaypointMission mission = buildMission(missionDetails, missionProxy.getPathPoints(), WaypointMissionFinishedAction.GO_HOME);
            if(mission != null){
                loadMission(mission);
            }
            if (surveyDetail.getCameraDetail().getName().contains("Agri")) {
                turnOffObstacleAvoidance();
            }
            if(!isValidTriggerSpeed()){
                intent = new Intent(ERROR_CAMERA);
                mainHandler.post(notifyStatus);
                return;
            }
            setCameraWhiteBalance(surveyDetail);
        }

        setMissionListener();
        uploadMission();
        setCameraMode();
        setAspectRatio();
    }
*/
    public WaypointMission buildMission(MissionDetails missionDetails, List<LatLong> points, WaypointMissionFinishedAction action){
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder();
        //get mission parameters
        float flightSpeed = missionDetails.getSpeed();
        float imageDistance = missionDetails.getImageDistance();
        float altitude = missionDetails.getAltitude();

        //generate list of waypoint objects from lat, long, altitude
        List<Waypoint> waypointList = new ArrayList<>();
        for (LatLong point : points) {
            LatLng pointLatLng = new LatLng(point.getLatitude(), point.getLongitude());
            Waypoint mWaypoint = new Waypoint(pointLatLng.latitude, pointLatLng.longitude, altitude);
            waypointList.add(mWaypoint);
        }

        //add waypoints to builder
        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        waypointMissionBuilder.finishedAction(action)
                .flightPathMode(WaypointMissionFlightPathMode.CURVED)
                .headingMode(WaypointMissionHeadingMode.AUTO)
                .autoFlightSpeed(flightSpeed)
                .maxFlightSpeed(flightSpeed);
        //shootPhotoTimeInterval not supported on older firmware versions
        if (DroidPlannerApp.isFirmwareNewVersion() != null && DroidPlannerApp.isFirmwareNewVersion()){
            waypointMissionBuilder.setGimbalPitchRotationEnabled(true);
            if (waypointMissionBuilder.getWaypointList().size() > 0) {
                for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                    waypointMissionBuilder.getWaypointList().get(i).shootPhotoDistanceInterval
                            = imageDistance;
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
        getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
        getWaypointMissionOperator().addListener(waypointMissionOperatorListener);
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

    /*
    Mission controls
     */
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
                    returnToLaunch();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(context.getString(R.string.mission_aborted), true);
                    editor.apply();
                    intent = new Intent(MiSSION_STOP);
                    mainHandler.post(notifyStatus);
                    getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
                } else {
                    Toast.makeText(context,error.getDescription(), Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void startTimelineMission() {
        if (MissionControl.getInstance().scheduledCount() > 0) {
            MissionControl.getInstance().startTimeline();
        }
    }

    public void stopTimelineMission() {
        MissionControl.getInstance().stopTimeline();
        returnToLaunch();
    }

    private void returnToLaunch() {
        /**
         * Command aircraft to return to home position
         */
        KeyManager.getInstance().performAction(goHomeKey, new ActionCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(DJIError djiError) {
                Toast.makeText(context, djiError.getDescription(), Toast.LENGTH_LONG).show();
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

    private float getImageDistance() {
        return (float) getSurveyDetail().getLongitudinalPictureDistance();
    }

    /*

     */

    public List<LatLong> getWaypointsFromString(String waypointsString, int startWaypoint) {
        /**
         * Extract waypoints from space separated string
         */
        if (startWaypoint != 0) {
            startWaypoint = startWaypoint - 1;
        }
        String[] stringPoints = waypointsString.split(" ");
        List<LatLong> latLongList = new ArrayList<>();
        for (String point : stringPoints) {
            String[] latLong = point.split(",");
            latLongList.add(new LatLong(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1])));
        }
        return latLongList.subList(startWaypoint, latLongList.size());
    }

    public String convertWaypointToString(List<LatLong> points) {
        String pointsString = "";
        for (LatLong point : points) {
            pointsString = pointsString.concat(String.format(Locale.ENGLISH, "%f,%f ", point.getLatitude(), point.getLongitude()));
        }
        return pointsString;
    }

    public MissionDetails getCurrentMissionDetails(List<LatLong> points, float speed, float imageDistance, float altitude) {
        MissionDetails missionDetails = new MissionDetails();
        missionDetails.setSpeed(speed);
        missionDetails.setWaypoints(convertWaypointToString(points));
        missionDetails.setImageDistance(imageDistance);
        missionDetails.setAltitude(altitude);
        return missionDetails;
    }

    public List<MissionDetails> getPreviousMissionDetails(Context context) {
        SQLiteDatabaseHandler db = new SQLiteDatabaseHandler(context);
        List<MissionDetails> missionDetailsList = db.getAllMissionDetails();
        if (missionDetailsList.isEmpty()) {
            return null;
        } else {
            return missionDetailsList;

        }
    }

    public void saveMissionDetails(Context context, float altitude, float imageDistance, String waypoints, float speed) {
        MissionDetails missionDetails = new MissionDetails(waypoints, altitude, imageDistance, speed);
        List<MissionDetails> missionDetailsList = new ArrayList<>();
        missionDetailsList.add(missionDetails);
        new SQLiteDatabaseHandler(context).addMissionDetails(missionDetailsList);
    }

    public void deletePreviousMissions(Context context) {
        /**
         * delete mission details from db
         */
        new SQLiteDatabaseHandler(context).deleteMissionDetails();
    }
    /*
    Check Methods
     */

    private boolean isValidTriggerSpeed(float cameraDistance, float flightSpeed){
        return (cameraDistance / flightSpeed) >=2f;
    }

    /*
    Camera Controls
     */

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


    /*
    Gimbal controls
     */
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


    private void turnOffObstacleAvoidance() {
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

    /*
    Old firmware camera controls
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
                        //setResultToToast("Camera Mode Set");
                    }
                }
            });

            camera.setPhotoTimeIntervalSettings(photoTimeIntervalSettings, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        //setResultToToast("Photo Interval Set");
                    }
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

}
