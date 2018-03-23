package co.aerobotics.android.mission;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.data.SQLiteDatabaseHandler;
import co.aerobotics.android.media.ImageImpl;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionState;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.Mission;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by michaelwootton on 8/25/17.
 */

public class TimelineMissionImpl extends DJIMissionImpl {

    private MissionProxy missionProxy;
    private Intent intent;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context context;
    private SharedPreferences sharedPreferences;

    public TimelineMissionImpl() {
    }

    public void runTimelineMission(MissionProxy missionProxy, Context context, boolean resume) {
        this.missionProxy = missionProxy;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(context.getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        if (!missionProxy.getItems().isEmpty()) {
            buildMultiBoundaryMission(resume);
            if (!resume) {
                SurveyDetail surveyDetail = ((Survey) missionProxy.getItems().get(0).getMissionItem()).getSurveyDetail();
                setCameraWhiteBalance(surveyDetail);
                if (surveyDetail.getCameraDetail().getName().contains("Agri")) {
                    //turnOffObstacleAvoidance();
                }
            }
            setAspectRatio();
            setCameraMode();
            startTimelineMission();
        }
    }

    private WaypointMissionOperatorListener waypointMissionOperatorListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent waypointMissionUploadEvent) {
            if (waypointMissionUploadEvent.getCurrentState().equals(WaypointMissionState.READY_TO_EXECUTE)) {
                //rotateGimbal(-90.0f, 0.1);
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
            Toast.makeText(context, "here", Toast.LENGTH_SHORT).show();
            if (waypointMissionExecutionEvent.getProgress() != null) {
                Toast.makeText(context, "update", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(context.getString(R.string.last_waypoint_index), waypointMissionExecutionEvent.getProgress().targetWaypointIndex);
                editor.apply();
            } else{
                Toast.makeText(context, "null", Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        public void onExecutionStart() {
            Toast.makeText(context, "starting mission", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(context.getString(R.string.survey_index), MissionControl.getInstance().getCurrentTimelineMarker());
            editor.apply();
            if (MissionControl.getInstance().getCurrentTimelineMarker() == 0) {
                intent = new Intent(DJIMissionImpl.MISSION_START);
                mainHandler.post(notifyStatus);
            }
        }

        @Override
        public void onExecutionFinish(DJIError djiError) {
            /*
            if (MissionControl.getInstance().getCurrentTimelineMarker() == MissionControl.getInstance().scheduledCount() - 1) {
                waypoint = 0;
                intent = new Intent(DJIMissionImpl.MiSSION_STOP);
                mainHandler.post(notifyStatus);
            }*/
        }
    };

    private MissionControl.Listener listener = new MissionControl.Listener() {
        @Override
        public void onEvent(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {
            updateTimelineStatus(element, event, error);
        }
    };


    private void updateTimelineStatus(TimelineElement element, TimelineEvent event, final DJIError error) {

        switch (event) {
            case STARTED:
                // null element refers to global event
                if (element == null) {
                    imageImpl = new ImageImpl();
                    rotateGimbal(-90, 0.1);
                    setWaypointListener();
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
                    MissionControl.getInstance().getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
                }
                break;
            case START_ERROR:
                break;
            default:
                break;

        }
    }

    private Runnable notifyStatus = new Runnable() {
        @Override
        public void run() {
            LocalBroadcastManager.getInstance(DroidPlannerApp.getInstance()).sendBroadcast(intent);
        }
    };

    private void buildMultiBoundaryMission(boolean resume) {
        List<MissionDetails> missionsToSurvey;
        if (resume) {
            missionsToSurvey = getMissionDetailsFromDb(context, sharedPreferences);
        } else {
            missionsToSurvey = getMissionDetailsFromMissionProxyItems(missionProxy);
        }
        //delete missions from db
        deletePreviousMissions(context);
        //save the new missions to db
        saveMissionDetailsToDb(context, missionsToSurvey);
        //if more than one survey area then setup timeline mission
        List<WaypointMission> waypointMissions = getWaypointMissionList(missionsToSurvey);
        if (waypointMissions.size() > 0) {
            List<TimelineElement> elements = getTimelineElements(waypointMissions);

            MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
            if (missionControl.scheduledCount() > 0) {
                missionControl.unscheduleEverything();
                missionControl.removeAllListeners();
            }

            missionControl.scheduleElements(elements);
            missionControl.addListener(listener);
        } else {
            WaypointMission mission = waypointMissions.get(0);
            if(mission != null){
                //loadMission(mission);
            }
        }


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
                MissionDetails missionDetails = getCurrentMissionDetails(points, speed, imageDistance, altitude);
                missionsToSurvey.add(missionDetails);
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
                waypointMissions.add(buildMission(missionsToSurvey.get(i),
                        getWaypointsFromString(missionsToSurvey.get(i).getWaypoints(), 0),
                        WaypointMissionFinishedAction.GO_HOME));
            } else {
                waypointMissions.add(buildMission(missionsToSurvey.get(i),
                        getWaypointsFromString(missionsToSurvey.get(i).getWaypoints(), 0),
                        WaypointMissionFinishedAction.NO_ACTION));
            }
        }
        return waypointMissions;
    }

    private void startTimelineMission() {
        if (MissionControl.getInstance().scheduledCount() > 0) {
            MissionControl.getInstance().startTimeline();
        }


        if (!DroidPlannerApp.isFirmwareNewVersion()) {
            initCamera((int) getCameraTriggerSpeed(missionProxy));
            MissionControl.getInstance().getWaypointMissionOperator().addListener(new WaypointMissionOperatorListener() {
                @Override
                public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent waypointMissionDownloadEvent) {

                }

                @Override
                public void onUploadUpdate(@NonNull WaypointMissionUploadEvent waypointMissionUploadEvent) {

                }

                @Override
                public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
                    if (waypointMissionExecutionEvent.getProgress() != null && waypointMissionExecutionEvent.getProgress().targetWaypointIndex == 1) {
                        if (!cameraStarted && !DroidPlannerApp.isFirmwareNewVersion()) {
                            cameraStarted = true;
                            startCamera();
                            MissionControl.getInstance().getWaypointMissionOperator().removeListener(this);
                        }
                    }
                }

                @Override
                public void onExecutionStart() {
                    rotateGimbal(-90.0f, 0.1);
                }

                @Override
                public void onExecutionFinish(@Nullable DJIError djiError) {

                }
            });
        }
    }
    private void setWaypointListener(){
        MissionControl.getInstance().getWaypointMissionOperator().removeListener(waypointMissionOperatorListener);
        MissionControl.getInstance().getWaypointMissionOperator().addListener(waypointMissionOperatorListener);
    }
    public void stopTimelineMission() {
        MissionControl.getInstance().stopTimeline();
        //goHome();
    }

    private float getCameraTriggerSpeed(MissionProxy missionProxy) {
        SurveyDetail surveyDetail = ((Survey) missionProxy.getItems().get(0).getMissionItem()).getSurveyDetail();
        double cameraDistance = surveyDetail.getLongitudinalPictureDistance();
        return (float) (cameraDistance / surveyDetail.getSpeed());
    }
}