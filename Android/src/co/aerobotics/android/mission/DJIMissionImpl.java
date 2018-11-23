package co.aerobotics.android.mission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.data.SQLiteDatabaseHandler;
import co.aerobotics.android.maps.GoogleMapFragment;
import co.aerobotics.android.media.ImageImpl;
import co.aerobotics.android.proxy.mission.MissionProxy;

import com.getkeepsafe.taptargetview.TapTarget;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.squareup.okhttp.internal.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import co.aerobotics.android.utils.location.EGM96;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Angle;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.LatLon;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.WhiteBalance;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.SmartRTHState;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
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
import dji.sdk.mission.timeline.TimelineMission;
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
            Log.i(TAG, "RTH " + rthState.toString());
            if (!isTimelineMission) {
                rotateGimbal(0, 0.5);
                stopCamera();
                intent = new Intent(MiSSION_STOP);
                intent.putExtra("rth_state", rthState.toString());
                mainHandler.post(notifyStatus);
                getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
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

    public void initializeMission(MissionProxy missionProxy, final Context context, boolean resume) {
        if (DroidPlannerApp.isFirmwareNewVersion() == null) {
            DroidPlannerApp.getInstance().getFirmwareVersion();
        }

        // COMMENT OUT FOR DEBUGGING WITHOUT DRONE
        final FlightController flightController = ((Aircraft) DroidPlannerApp.getProductInstance()).getFlightController();
        flightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(FlightControllerState flightControllerState) {
                rthState = flightControllerState.getGoHomeAssessment().getSmartRTHState();
                if (rthState != null && (rthState.equals(SmartRTHState.EXECUTED))) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(context.getString(R.string.mission_aborted), true);
                    editor.apply();
                    flightController.setStateCallback(null);
                }
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
        if (waypointMissions.size() > 1) {
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

        // UNCOMMENT FOR DEPLOYMENT WITH DRONE
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
            elements.add(TimelineMission.elementFromWaypointMission(mission));
        }
        return elements;
    }

    private void saveMissionDetailsToDb(Context context, List<MissionDetails> missionDetailsList) {
        new SQLiteDatabaseHandler(context).addMissionDetails(missionDetailsList);
    }

    private List<MissionDetails> getMissionDetailsFromMissionProxyItems(MissionProxy missionProxy) {
        // COMMENT OUT FOR DEBUGGING WITHOUT DRONE
        final FlightController flightController = ((Aircraft) DroidPlannerApp.getProductInstance()).getFlightController();
        flightController.setMaxFlightHeight(499, null);
        flightController.setMaxFlightRadius(7999, null);

        /**
         * Return list of MissionDetails objects from mission proxy
         */
        List<MissionItemProxy> items = missionProxy.getItems();
        List<MissionDetails> missionsToSurvey = new ArrayList<>();

        // FOR TESTING WITHOUT DRONE
        // close to lower block on lower slopes farm
//            double currentLat = -33.94825;
//            double currentLong = 18.408898;
        // Aerobotics
//            double currentLat = -33.930212;
//            double currentLong = 18.443295;

        LatLong currentCoords = new LatLong(flightController.getState().getAircraftLocation().getLatitude(), flightController.getState().getAircraftLocation().getLongitude());

        double takeoffAltitude = getTakeOffAltitude(items, currentCoords);
        double returnToLaunchAlt = 20;

        for (MissionItemProxy itemProxy : items) {
            MissionItem item = itemProxy.getMissionItem();
            // List of takeoff-referenced heights for each waypoint on the ground (i.e. takeoff is 0, not 0 + altitude)
            List<Double> gridPointHeights = new ArrayList<Double>();

            if (item instanceof Survey) {
                List<LatLong> gridPoints = ((Survey) item).getGridPoints();
                List<LatLong> polygonPoints = ((Survey) item).getPolygonPoints();
                List<Double> polygonPointAltitudes = ((Survey) item).getPolygonPointAltitudes();
                float altitude = (float) ((Survey) item).getSurveyDetail().getAltitude();
                float speed = (float) ((Survey) item).getSurveyDetail().getSpeed();
                float imageDistance = (float) ((Survey) item).getSurveyDetail().getLongitudinalPictureDistance();

                /*
                 *  Add offset to flight altitude to allow for terrain irregularity.
                 *  Requires polygonPointAltitudes to exist in survey object
                 */
                if (polygonPointAltitudes != null && polygonPointAltitudes.size() != 0) {
                    // Add offset to ensure drone maintains sufficient alitude throughout mission
                    //double altitudeOffset = highestAlt - takeoffAltitude;
                    //altitude += altitudeOffset;

                    // List of altitudes for each waypoint
                    List<Double> gridPointAltitudes = findAltitudesOnBoundary(gridPoints, polygonPoints, polygonPointAltitudes);

                    for (Double alt : gridPointAltitudes) {
                        gridPointHeights.add(alt - takeoffAltitude);
                    }

                } else {
                    for (int i = 0; i < gridPoints.size(); i++) {
                        gridPointHeights.add(0.0);
                    }
                }

                if (isValidTriggerSpeed(imageDistance, speed)) {
                    MissionDetails missionDetails = getCurrentMissionDetails(gridPoints, speed, imageDistance, altitude, gridPointHeights);

                    returnToLaunchAlt = Math.max(getHighestWaypoint(missionDetails), returnToLaunchAlt);
                    // COMMENT OUT FOR TESTING WITHOUT DRONE
                    flightController.setGoHomeHeightInMeters((int) Math.ceil(returnToLaunchAlt), null);

                    missionsToSurvey.add(missionDetails);
                } else {
                    return null;
                }

            }
        }

        return  missionsToSurvey;

    }

    private double getHighestWaypoint(MissionDetails missionDetails) {
        double highest = 0;

        if (missionDetails.getWaypointAltitudes() != null) {
            for (double waypointAlt : getWaypointsAltitudesFromString(missionDetails.getWaypointAltitudes(), 0))
                highest = Math.max(highest, waypointAlt);
        }

        return highest;
    }

    private double getTakeOffAltitude(List<MissionItemProxy> items, LatLong currentCoords) {
        double takeoffAltitude = 0;

        double currentLong = currentCoords.getLongitude();
        double currentLat = currentCoords.getLatitude();

        if (items.get(0).getMissionItem() instanceof Survey) {

            // Find polygonPoint closest to current GPS location (to be used to approximate takeoff altitude)
            LatLong closestPoint = ((Survey) items.get(0).getMissionItem()).getPolygonPoints().get(0);
            double closestPointAltitude = 0;
            float[] dist = {0};
            Location.distanceBetween(currentLong, currentLat, closestPoint.getLongitude(), closestPoint.getLatitude(), dist);
            double distToClosestPoint = dist[0];

            for (MissionItemProxy itemProxy : items) {
                MissionItem item = itemProxy.getMissionItem();

                if (item instanceof Survey) {
                    List<LatLong> polygonPoints = ((Survey) item).getPolygonPoints();
                    List<Double> polygonPointAltitudes = ((Survey) item).getPolygonPointAltitudes();

                    if (polygonPointAltitudes != null && polygonPointAltitudes.size() != 0) {

                        int i = 0;
                        for (LatLong polygonPoint : polygonPoints) {
                            Location.distanceBetween(currentLong, currentLat, polygonPoints.get(i).getLongitude(), polygonPoints.get(i).getLatitude(), dist);
                            double distToPoint = dist[0];

                            if (distToPoint < distToClosestPoint) {
                                distToClosestPoint = distToPoint;
                                closestPoint = polygonPoint;
                                closestPointAltitude = polygonPointAltitudes.get(i);
                            }
                            i += 1;
                        }

                        // Find highest polygonPoint - the point for where worst-case overlap will happen
                        Double highestAlt = polygonPointAltitudes.get(0);
                        for (Double polygonPointAlt : polygonPointAltitudes) {
                            highestAlt = Math.max(highestAlt, polygonPointAlt);
                        }

                    }
                }
            }

            //Location location = LocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location.distanceBetween(currentLong, currentLat, closestPoint.getLongitude(), closestPoint.getLatitude(), dist);
            distToClosestPoint = dist[0];

            if (GoogleMapFragment.location != null && distToClosestPoint > 25) { //check closest polygon point further than 25m away
                LatLong loc = new LatLong(GoogleMapFragment.location.getLatitude(), GoogleMapFragment.location.getLongitude());
                double geoidOffset = getGeoidAltitudeOffset(loc);
                takeoffAltitude = GoogleMapFragment.location.getAltitude() - geoidOffset;
            } else {
                takeoffAltitude = closestPointAltitude; // could use Google Elevation API to get altitude at currentCoords
            }

            return takeoffAltitude;
        }

        else {
            return 0;
        }
    }

    private double getGeoidAltitudeOffset(LatLong location) {
        EGM96 egm96 = null;
        try {
            egm96 = new EGM96("WW15MGH.DAC", this.context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double offset = 0;

        LatLon latLon = LatLon.fromDegrees(location.getLatitude(), location.getLongitude());
        offset = egm96.getOffset(latLon.latitude, latLon.longitude);
        return offset;
    }

    private List<Double> findAltitudesOnBoundary(List<LatLong> gridPoints, List<LatLong> polygonPoints, List<Double> polygonPointAltitudes) {
        List<Double> altitudes = new ArrayList<Double>();

        for (LatLong gridPoint : gridPoints) {
            for (int i = 0; i < polygonPoints.size(); i++) {
                if (i != polygonPoints.size() - 1) {
                    if (pointOnLine(gridPoint, polygonPoints.get(i), polygonPoints.get(i+1))) {
                        altitudes.add(altitudeOnBoundary(gridPoint, new LatLongAlt(polygonPoints.get(i), polygonPointAltitudes.get(i)), new LatLongAlt(polygonPoints.get(i+1), polygonPointAltitudes.get(i+1))));
                        break;
                    }
                } else { // last point - wrap around to first one
                    if (pointOnLine(gridPoint, polygonPoints.get(i), polygonPoints.get(0))) {
                        altitudes.add(altitudeOnBoundary(gridPoint, new LatLongAlt(polygonPoints.get(i), polygonPointAltitudes.get(i)), new LatLongAlt(polygonPoints.get(0), polygonPointAltitudes.get(0))));
                        break;
                    }
                }
            }
        }

        // In case something goes wrong, fill missing altitudes with last value
        while (altitudes.size() < gridPoints.size()) {
            altitudes.add(altitudes.get(altitudes.size()-1));
        }

        return altitudes;
    }

    private boolean pointOnLine(LatLong poi, LatLong pointA, LatLong pointB) {
        /*
         * returns a boolean of whether POI lies on or close to the line between point A and point B
         */

        double tolerance = Math.pow(10,-5); // Roughly a meter at the equator, less elsewhere
        boolean onLine;

        onLine = distanceBetweenCoords(poi, pointA) + distanceBetweenCoords(poi, pointB) - distanceBetweenCoords(pointA, pointB) < tolerance;

        return onLine;
    }

    // Could be put somewhere else as a general public helper - Dev
    private double distanceBetweenCoords(LatLong coordA, LatLong coordB) {
        /*
         * Returns distance between two LatLong pairs, in degrees
         * sqrt( (ax-bx)^2 + (ay-by)^2 )
         */

        double latdiff = coordA.getLatitude() - coordB.getLatitude();
        double longdiff = coordA.getLongitude() - coordB.getLongitude();

        return Math.sqrt(Math.pow(latdiff,2) + Math.pow(longdiff,2));
    }

    private double altitudeOnBoundary(LatLong pointOfInterest, LatLongAlt pointA, LatLongAlt pointB) {
        /*
         * returns the estimated altitude of a point - assuming it lies on the line between point A and point B
         */

        double diffX = Math.abs(pointA.getLongitude() - pointB.getLongitude());
        double diffY = Math.abs(pointA.getLatitude() - pointB.getLatitude());
        double diffZ = Math.abs(pointA.getAltitude() - pointB.getAltitude());
        double alt;
        double m = 0;

        // z = m(x-x0) + z0

        if (diffX != 0) {
            m = diffZ/diffX;
            alt = m * (pointOfInterest.getLongitude() - pointA.getLongitude()) + pointA.getAltitude();
        } else if (diffY != 0) {
            m = diffZ/diffY;
            alt = m * (pointOfInterest.getLatitude() - pointA.getLatitude()) + pointA.getAltitude();
        } else { // pointA and point B are at the same place...
            alt = pointA.getAltitude();
            return alt;
        }

        return alt;
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
                        getWaypointsAltitudesFromString(missionsToSurvey.get(i).getWaypointAltitudes(), 0),
                        WaypointMissionFinishedAction.GO_HOME);
                if (mission != null) {
                    waypointMissions.add(mission);
                }
            } else {
                WaypointMission mission = buildMission(missionsToSurvey.get(i),
                        getWaypointsFromString(missionsToSurvey.get(i).getWaypoints(), 0),
                        getWaypointsAltitudesFromString(missionsToSurvey.get(i).getWaypointAltitudes(), 0),
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
    public WaypointMission buildMission(MissionDetails missionDetails, List<LatLong> points, List<Double> pointAltitudes, WaypointMissionFinishedAction action){
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder();
        // anticipating waypoint points, not polygon vertices

        //get mission parameters
        float flightSpeed = missionDetails.getSpeed();
        float imageDistance = missionDetails.getImageDistance();
        float altitude = missionDetails.getAltitude();

        //generate list of waypoint objects from lat, long, altitude
        List<Waypoint> waypointList = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            double lat = points.get(i).getLatitude();
            double lng = points.get(i).getLongitude();
            float alt = pointAltitudes.get(i).floatValue();
            Waypoint mWaypoint = new Waypoint(lat, lng, alt + altitude);
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
            float triggerSpeed = imageDistance/flightSpeed;
            initCamera((int) triggerSpeed);
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

    public List<Double> getWaypointsAltitudesFromString(String waypointsString, int startWaypoint) {
        /**
         * Extract waypoint altitudes from comma separated string
         */
        if (startWaypoint != 0) {
            startWaypoint = startWaypoint - 1;
        }
        String[] stringPointAlts = waypointsString.split(",");
        List<Double> waypointAltList = new ArrayList<>();
        for (String pointAlt : stringPointAlts) {
            waypointAltList.add(Double.parseDouble(pointAlt));
        }
        return waypointAltList.subList(startWaypoint, waypointAltList.size());
    }

    public String convertWaypointToString(List<LatLong> points) {
        String pointsString = "";
        for (LatLong point : points) {
            pointsString = pointsString.concat(String.format(Locale.ENGLISH, "%f,%f ", point.getLatitude(), point.getLongitude()));
        }
        return pointsString;
    }

    public String convertDoubleListToString(List <Double> items) {
        String itemString = "";
        for (Double item : items) {
            itemString = itemString.concat(String.format(Locale.ENGLISH, "%f,", item));
        }
        return itemString;
    }

    public MissionDetails getCurrentMissionDetails(List<LatLong> gridPoints, float speed, float imageDistance, float altitude, List<Double> gridPointAltitudes) {
        MissionDetails missionDetails = new MissionDetails();
        missionDetails.setSpeed(speed);
        missionDetails.setWaypoints(convertWaypointToString(gridPoints));
        missionDetails.setImageDistance(imageDistance);
        missionDetails.setAltitude(altitude);
        missionDetails.setWaypointAltitudes(convertDoubleListToString(gridPointAltitudes));
        return missionDetails;
    }

    public List<MissionDetails> getPreviousMissionDetails(Context context) {
        // Won't have point altitudes...
        SQLiteDatabaseHandler db = new SQLiteDatabaseHandler(context);
        List<MissionDetails> missionDetailsList = db.getAllMissionDetails();
        if (missionDetailsList.isEmpty()) {
            return null;
        } else {
            return missionDetailsList;

        }
    }

    public void saveMissionDetails(Context context, float altitude, float imageDistance, String waypoints, float speed, String waypointAltitudes) {
        MissionDetails missionDetails = new MissionDetails(waypoints, altitude, imageDistance, speed, waypointAltitudes);
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
