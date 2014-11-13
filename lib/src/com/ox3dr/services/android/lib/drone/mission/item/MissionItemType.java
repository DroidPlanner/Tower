package com.ox3dr.services.android.lib.drone.mission.item;

import com.ox3dr.services.android.lib.drone.mission.item.command.CameraTrigger;
import com.ox3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.ox3dr.services.android.lib.drone.mission.item.command.EpmGripper;
import com.ox3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.ox3dr.services.android.lib.drone.mission.item.command.SetServo;
import com.ox3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.ox3dr.services.android.lib.drone.mission.item.command.YawCondition;
import com.ox3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.ox3dr.services.android.lib.drone.mission.item.raw.MissionItemMessage;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Land;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.ox3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

/**
 * /**
 * List of mission item types.
 */
public enum MissionItemType {

    WAYPOINT("Waypoint") {
        @Override
        public MissionItem getNewItem() {
            return new Waypoint();
        }
    },

    SPLINE_WAYPOINT("Spline Waypoint") {
        @Override
        public MissionItem getNewItem() {
            return new SplineWaypoint();
        }
    },

    TAKEOFF("Takeoff") {
        @Override
        public MissionItem getNewItem() {
            return new Takeoff();
        }
    },

    CHANGE_SPEED("Change Speed") {
        @Override
        public MissionItem getNewItem() {
            return new ChangeSpeed();
        }
    },

    CAMERA_TRIGGER("Camera Trigger") {
        @Override
        public MissionItem getNewItem() {
            return new CameraTrigger();
        }
    },

    EPM_GRIPPER("EPM Gripper") {
        @Override
        public MissionItem getNewItem() {
            return new EpmGripper();
        }
    },

    RETURN_TO_LAUNCH("Return to Launch") {
        @Override
        public MissionItem getNewItem() {
            return new ReturnToLaunch();
        }
    },

    LAND("Land") {
        @Override
        public MissionItem getNewItem() {
            return new Land();
        }
    },

    CIRCLE("Circle") {
        @Override
        public MissionItem getNewItem() {
            return new Circle();
        }
    },

    REGION_OF_INTEREST("Region of Interest") {
        @Override
        public MissionItem getNewItem() {
            return new RegionOfInterest();
        }
    },

    SURVEY("Survey") {
        @Override
        public MissionItem getNewItem() {
            return new Survey();
        }
    },

    STRUCTURE_SCANNER("Structure Scanner") {
        @Override
        public MissionItem getNewItem() {
            return new StructureScanner();
        }
    },

    SET_SERVO("Set Servo") {
        @Override
        public MissionItem getNewItem() {
            return new SetServo();
        }
    },

    YAW_CONDITION("Set Yaw"){
        @Override
        public MissionItem getNewItem(){
            return new YawCondition();
        }
    };

    private final String label;

    private MissionItemType(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }

    public abstract MissionItem getNewItem();
}
