package co.aerobotics.android.mission;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import co.aerobotics.android.DroidPlannerApp;
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
import dji.sdk.camera.Camera;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.Mission;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineElementFeedback;
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
    private int waypoint = 0;

    public TimelineMissionImpl() {
    }

    public void run(MissionProxy missionProxy) {
        this.missionProxy = missionProxy;
        if (!missionProxy.getItems().isEmpty()) {

            buildMultiBoundaryMission();
            SurveyDetail surveyDetail = ((Survey) missionProxy.getItems().get(0).getMissionItem()).getSurveyDetail();
            setCameraWhiteBalance(surveyDetail);
            if (surveyDetail.getCameraDetail().getName().contains("Agri")) {
                turnOffObstacleAvoidance();
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
                    MissionControl.getInstance().getWaypointMissionOperator().removeListener(this);
                }
            }
        }

        @Override
        public void onExecutionStart() {
            if (MissionControl.getInstance().getCurrentTimelineMarker() == 0) {
                waypoint = 0;
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

    private void buildMultiBoundaryMission() {

        List<MissionItemProxy> items = missionProxy.getItems();
        List<List<LatLong>> surveys = new ArrayList<>();
        for (MissionItemProxy itemProxy : items) {
            MissionItem item = itemProxy.getMissionItem();
            if (item instanceof Survey) {
                surveys.add(((Survey) item).getGridPoints());
            }
        }

        List<WaypointMission> waypointMissions = new ArrayList<>();
        for (int i = 0; i < surveys.size(); i++) {
            SurveyDetail surveyDetail = ((Survey) items.get(i).getMissionItem()).getSurveyDetail();
            if (i == surveys.size() - 1) {
                waypointMissions.add(buildWaypointMission(surveys.get(i), surveyDetail, WaypointMissionFinishedAction.GO_HOME));
            } else {
                waypointMissions.add(buildWaypointMission(surveys.get(i), surveyDetail, WaypointMissionFinishedAction.NO_ACTION));
            }

        }

        List<TimelineElement> elements = new ArrayList<>();
        for (WaypointMission mission : waypointMissions) {
            elements.add(Mission.elementFromWaypointMission(mission));
        }

        MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
        }

        missionControl.scheduleElements(elements);
        missionControl.addListener(listener);

    }

    private WaypointMission buildWaypointMission(List<LatLong> points, SurveyDetail surveyDetail, WaypointMissionFinishedAction action) {
        //List<LatLong> points =  missionProxy.getPathPoints();
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder();
        List<Waypoint> waypointList = new ArrayList<>();

        for (LatLong point : points) {
            LatLng pointLatLng = new LatLng(point.getLatitude(), point.getLongitude());
            Waypoint mWaypoint = new Waypoint(pointLatLng.latitude, pointLatLng.longitude, (float) surveyDetail.getAltitude());
            waypointList.add(mWaypoint);
        }
        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        waypointMissionBuilder.finishedAction(action)
                .flightPathMode(WaypointMissionFlightPathMode.CURVED)
                .headingMode(WaypointMissionHeadingMode.AUTO)
                .autoFlightSpeed((float) surveyDetail.getSpeed())
                .maxFlightSpeed((float) surveyDetail.getSpeed());

        //shootPhotoTimeInterval not supported on older firmware versions
        if (DroidPlannerApp.isFirmwareNewVersion()) {
            waypointMissionBuilder.setGimbalPitchRotationEnabled(true);
            if (waypointMissionBuilder.getWaypointList().size() > 0) {
                for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                    waypointMissionBuilder.getWaypointList().get(i).shootPhotoDistanceInterval
                            = (float) surveyDetail.getLongitudinalPictureDistance();
                    waypointMissionBuilder.getWaypointList().get(i).cornerRadiusInMeters = 0.3f;
                    waypointMissionBuilder.getWaypointList().get(i).gimbalPitch = -90f;

                }
            }
        } else {
            initCamera((int) surveyDetail.getLongitudinalPictureDistance());
        }

        DJIError check = waypointMissionBuilder.checkParameters();
        if (check == null) {
            return waypointMissionBuilder.build();
        } else {
            return null;
        }
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
        goHome();
    }

    private long getNumberofImages() {
        List<Survey> surveyList = new ArrayList<>();
        for (MissionItemProxy item : missionProxy.getItems()) {
            surveyList.add((Survey) item.getMissionItem());
        }
        long totalImages = 0;
        for (Survey survey : surveyList) {
            totalImages = totalImages + survey.getCameraCount();
        }
        return totalImages;
    }

    private float getCameraTriggerSpeed(MissionProxy missionProxy) {
        SurveyDetail surveyDetail = ((Survey) missionProxy.getItems().get(0).getMissionItem()).getSurveyDetail();
        double cameraDistance = surveyDetail.getLongitudinalPictureDistance();
        return (float) (cameraDistance / surveyDetail.getSpeed());
    }
}