package org.droidplanner.services.android.impl.core.mission.survey;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;
import org.droidplanner.services.android.impl.core.mission.commands.CameraTriggerImpl;
import org.droidplanner.services.android.impl.core.polygon.Polygon;
import org.droidplanner.services.android.impl.core.survey.CameraInfo;
import org.droidplanner.services.android.impl.core.survey.SurveyData;
import org.droidplanner.services.android.impl.core.survey.grid.Grid;
import org.droidplanner.services.android.impl.core.survey.grid.GridBuilder;

import java.util.ArrayList;
import java.util.List;

public class SurveyImpl extends MissionItemImpl {

    public Polygon polygon = new Polygon();
    public SurveyData surveyData = new SurveyData();
    public Grid grid;

    private boolean startCameraBeforeFirstWaypoint;

    public SurveyImpl(MissionImpl missionImpl, List<LatLong> points) {
        super(missionImpl);
        polygon.addPoints(points);
    }

    public void update(double angle, double altitude, double overlap, double sidelap, boolean lockOrientation) {
        surveyData.update(angle, altitude, overlap, sidelap, lockOrientation);
    }

    public boolean isStartCameraBeforeFirstWaypoint() {
        return startCameraBeforeFirstWaypoint;
    }

    public void setStartCameraBeforeFirstWaypoint(boolean startCameraBeforeFirstWaypoint) {
        this.startCameraBeforeFirstWaypoint = startCameraBeforeFirstWaypoint;
    }

    public void setCameraInfo(CameraInfo camera) {
        surveyData.setCameraInfo(camera);
    }

    public void build() throws Exception {
        // TODO find better point than (0,0) to reference the grid
        grid = null;
        GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new LatLong(0, 0));
        polygon.checkIfValid();
        grid = gridBuilder.generate(true);
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        try {
            List<msg_mission_item> list = new ArrayList<msg_mission_item>();
            build();

            packSurveyPoints(list);

            return list;
        } catch (Exception e) {
            return new ArrayList<msg_mission_item>();
        }
    }

    private void packSurveyPoints(List<msg_mission_item> list) {
        //Generate the camera trigger
        CameraTriggerImpl camTrigger = new CameraTriggerImpl(missionImpl, surveyData.getLongitudinalPictureDistance());

        //Add it if the user wants it to start before the first waypoint.
        if(startCameraBeforeFirstWaypoint){
            list.addAll(camTrigger.packMissionItem());
        }

        final double altitude = surveyData.getAltitude();

        //Add the camera trigger after the first waypoint if it wasn't added before.
        boolean addToFirst = !startCameraBeforeFirstWaypoint;

        for (LatLong point : grid.gridPoints) {
            msg_mission_item mavMsg = getSurveyPoint(point, altitude);
            list.add(mavMsg);
            if(surveyData.getLockOrientation()) {
                msg_mission_item yawMsg = getYawCondition(surveyData.getAngle());
                list.add(yawMsg);
            }

            if(addToFirst){
                list.addAll(camTrigger.packMissionItem());
                addToFirst = false;
            }
        }

        list.addAll((new CameraTriggerImpl(missionImpl, (0.0)).packMissionItem()));
    }

    protected msg_mission_item getSurveyPoint(LatLong point, double altitude){
        return packSurveyPoint(point, altitude);
    }

    public static msg_mission_item packSurveyPoint(LatLong point, double altitude) {
        msg_mission_item mavMsg = new msg_mission_item();
        mavMsg.autocontinue = 1;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
        mavMsg.x = (float) point.getLatitude();
        mavMsg.y = (float) point.getLongitude();
        mavMsg.z = (float) altitude;
        mavMsg.param1 = 0f;
        mavMsg.param2 = 0f;
        mavMsg.param3 = 0f;
        mavMsg.param4 = 0f;
        return mavMsg;
    }

    private msg_mission_item getYawCondition(double angle){
        msg_mission_item mavMsg = new msg_mission_item();
        mavMsg.autocontinue = 1;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_LOCAL_ENU;
        mavMsg.command = MAV_CMD.MAV_CMD_CONDITION_YAW;
        mavMsg.x = 0f;
        mavMsg.y = 0f;
        mavMsg.z = 0f;
        mavMsg.param1 = (float)angle;
        //yaw craft a 30 degrees per second (value is relatively insignificant since it only applies when approaching first waypoint of mission)
        mavMsg.param2 = 30;
        mavMsg.param3 = 0;
        mavMsg.param4 = 0;
        return mavMsg;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.SURVEY;
    }

}
