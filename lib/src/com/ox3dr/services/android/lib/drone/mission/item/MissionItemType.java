package com.ox3dr.services.android.lib.drone.mission.item;

import com.ox3dr.services.android.lib.drone.mission.item.command.CameraTrigger;
import com.ox3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.ox3dr.services.android.lib.drone.mission.item.command.EpmGripper;
import com.ox3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.ox3dr.services.android.lib.drone.mission.item.command.SetServo;
import com.ox3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.ox3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.ox3dr.services.android.lib.drone.mission.item.raw.MissionItemMessage;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Land;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.StructureScanner;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

/**
 * /**
 * List of mission item types.
 */
public enum MissionItemType {

    CAMERA_TRIGGER("Camera Trigger") {
        @Override
        public MissionItem getNewItem() {
            return new CameraTrigger();
        }
    },

    RAW_MESSAGE("Raw Message") {
        @Override
        public MissionItem getNewItem() {
            return new MissionItemMessage();
        }
    },

    CHANGE_SPEED("Change Speed") {
        @Override
        public MissionItem getNewItem() {
            return new ChangeSpeed();
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

    SET_SERVO("Set Servo") {
        @Override
        public MissionItem getNewItem() {
            return new SetServo();
        }
    },

    TAKEOFF("Takeoff") {
        @Override
        public MissionItem getNewItem() {
            return new Takeoff();
        }
    },

    CIRCLE("Circle") {
        @Override
        public MissionItem getNewItem() {
            return new Circle();
        }
    },

    LAND("Land") {
        @Override
        public MissionItem getNewItem() {
            return new Land();
        }
    },

    REGION_OF_INTEREST("Region of Interest") {
        @Override
        public MissionItem getNewItem() {
            return new RegionOfInterest();
        }
    },

    SPLINE_WAYPOINT("Spline Waypoint") {
        @Override
        public MissionItem getNewItem() {
            return new SplineWaypoint();
        }
    },

    STRUCTURE_SCANNER("Structure Scanner") {
        @Override
        public MissionItem getNewItem() {
            return new StructureScanner();
        }
    },

    WAYPOINT("Waypoint") {
        @Override
        public MissionItem getNewItem() {
            return new Waypoint();
        }
    },

    SURVEY("Survey") {
        @Override
        public MissionItem getNewItem() {
            return new Survey();
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
