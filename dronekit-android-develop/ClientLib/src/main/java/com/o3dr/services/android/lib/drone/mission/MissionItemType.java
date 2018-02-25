package com.o3dr.services.android.lib.drone.mission;

import android.os.Bundle;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.CameraTrigger;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.o3dr.services.android.lib.drone.mission.item.command.DoJump;
import com.o3dr.services.android.lib.drone.mission.item.command.EpmGripper;
import com.o3dr.services.android.lib.drone.mission.item.command.ResetROI;
import com.o3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.o3dr.services.android.lib.drone.mission.item.command.SetRelay;
import com.o3dr.services.android.lib.drone.mission.item.command.SetServo;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.o3dr.services.android.lib.drone.mission.item.command.YawCondition;
import com.o3dr.services.android.lib.drone.mission.item.complex.SplineSurvey;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.o3dr.services.android.lib.drone.mission.item.spatial.DoLandStart;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Land;
import com.o3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.util.ParcelableUtils;

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

        @Override
        protected Parcelable.Creator<Waypoint> getMissionItemCreator() {
            return Waypoint.CREATOR;
        }
    },

    SPLINE_WAYPOINT("Spline Waypoint") {
        @Override
        public MissionItem getNewItem() {
            return new SplineWaypoint();
        }

        @Override
        protected Parcelable.Creator<SplineWaypoint> getMissionItemCreator() {
            return SplineWaypoint.CREATOR;
        }
    },

    TAKEOFF("Takeoff") {
        @Override
        public MissionItem getNewItem() {
            return new Takeoff();
        }

        @Override
        protected Parcelable.Creator<Takeoff> getMissionItemCreator() {
            return Takeoff.CREATOR;
        }
    },

    CHANGE_SPEED("Change Speed") {
        @Override
        public MissionItem getNewItem() {
            return new ChangeSpeed();
        }

        @Override
        protected Parcelable.Creator<ChangeSpeed> getMissionItemCreator() {
            return ChangeSpeed.CREATOR;
        }
    },

    CAMERA_TRIGGER("Camera Trigger") {
        @Override
        public MissionItem getNewItem() {
            return new CameraTrigger();
        }

        @Override
        protected Creator<CameraTrigger> getMissionItemCreator() {
            return CameraTrigger.CREATOR;
        }
    },

    EPM_GRIPPER("EPM Gripper") {
        @Override
        public MissionItem getNewItem() {
            return new EpmGripper();
        }

        @Override
        protected Creator<EpmGripper> getMissionItemCreator() {
            return EpmGripper.CREATOR;
        }

    },

    RETURN_TO_LAUNCH("Return to Launch") {
        @Override
        public MissionItem getNewItem() {
            return new ReturnToLaunch();
        }

        @Override
        protected Creator<ReturnToLaunch> getMissionItemCreator() {
            return ReturnToLaunch.CREATOR;
        }
    },

    LAND("Land") {
        @Override
        public MissionItem getNewItem() {
            return new Land();
        }

        @Override
        protected Creator<Land> getMissionItemCreator() {
            return Land.CREATOR;
        }
    },

    CIRCLE("Circle") {
        @Override
        public MissionItem getNewItem() {
            return new Circle();
        }

        @Override
        protected Creator<Circle> getMissionItemCreator() {
            return Circle.CREATOR;
        }
    },

    REGION_OF_INTEREST("Region of Interest") {
        @Override
        public MissionItem getNewItem() {
            return new RegionOfInterest();
        }

        @Override
        protected Creator<RegionOfInterest> getMissionItemCreator() {
            return RegionOfInterest.CREATOR;
        }
    },

    SURVEY("Survey") {
        @Override
        public MissionItem getNewItem() {
            return new Survey();
        }

        @Override
        protected Creator<Survey> getMissionItemCreator() {
            return Survey.CREATOR;
        }
    },

    STRUCTURE_SCANNER("Structure Scanner") {
        @Override
        public MissionItem getNewItem() {
            return new StructureScanner();
        }

        @Override
        protected Creator<StructureScanner> getMissionItemCreator() {
            return StructureScanner.CREATOR;
        }
    },

    SET_SERVO("Set Servo") {
        @Override
        public MissionItem getNewItem() {
            return new SetServo();
        }

        @Override
        protected Creator<SetServo> getMissionItemCreator() {
            return SetServo.CREATOR;
        }
    },

    YAW_CONDITION("Set Yaw") {
        @Override
        public MissionItem getNewItem() {
            return new YawCondition();
        }

        @Override
        protected Creator<YawCondition> getMissionItemCreator() {
            return YawCondition.CREATOR;
        }
    },

    SET_RELAY("Set Relay") {
        @Override
        public MissionItem getNewItem() {
            return new SetRelay();
        }

        @Override
        protected Creator<SetRelay> getMissionItemCreator() {
            return SetRelay.CREATOR;
        }
    },

    DO_LAND_START("Do Land start") {
        @Override
        public boolean isTypeSupported(Type vehicleType){
            return super.isTypeSupported(vehicleType) && vehicleType.getDroneType() == Type.TYPE_PLANE;
        }

        @Override
        public MissionItem getNewItem() {
            return new DoLandStart();
        }

        @Override
        protected Creator<DoLandStart> getMissionItemCreator() {
            return DoLandStart.CREATOR;
        }
    },

    SPLINE_SURVEY("Spline Survey") {
        @Override
        public MissionItem getNewItem() {
            return new SplineSurvey();
        }

        @Override
        protected Creator<SplineSurvey> getMissionItemCreator() {
            return SplineSurvey.CREATOR;
        }
    },

    DO_JUMP("Do Jump") {
        @Override
        public MissionItem getNewItem() {
            return new DoJump();
        }

        @Override
        protected Creator<DoJump> getMissionItemCreator() {
            return DoJump.CREATOR;
        }
    },

    RESET_ROI("Reset ROI") {
        @Override
        public MissionItem getNewItem() {
            return new ResetROI();
        }

        @Override
        protected Creator<ResetROI> getMissionItemCreator() {
            return ResetROI.CREATOR;
        }
    };

    private final static String EXTRA_MISSION_ITEM_TYPE = "extra_mission_item_type";
    private final static String EXTRA_MISSION_ITEM = "extra_mission_item";

    private final String label;

    private MissionItemType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public abstract MissionItem getNewItem();

    public final Bundle storeMissionItem(MissionItem item) {
        Bundle bundle = new Bundle(2);
        storeMissionItem(item, bundle);
        return bundle;
    }

    public void storeMissionItem(MissionItem missionItem, Bundle bundle) {
        bundle.putString(EXTRA_MISSION_ITEM_TYPE, name());
        bundle.putByteArray(EXTRA_MISSION_ITEM, ParcelableUtils.marshall(missionItem));
    }

    protected abstract <T extends MissionItem> Creator<T> getMissionItemCreator();

    public static <T extends MissionItem> T restoreMissionItemFromBundle(Bundle bundle) {
        if (bundle == null)
            return null;

        String typeName = bundle.getString(EXTRA_MISSION_ITEM_TYPE);
        byte[] marshalledItem = bundle.getByteArray(EXTRA_MISSION_ITEM);
        if (typeName == null || marshalledItem == null)
            return null;

        MissionItemType type = MissionItemType.valueOf(typeName);
        if (type == null)
            return null;

        T missionItem = ParcelableUtils.unmarshall(marshalledItem, type.<T>getMissionItemCreator());
        return missionItem;
    }

    /**
     * Indicates if the mission item is supported on the given vehicle type.
     * @param vehicleType Vehicle type to check against (i.e: plane, copter, rover...)
     * @return true the mission item is supported, false otherwise.
     */
    public boolean isTypeSupported(Type vehicleType){
        return vehicleType != null;
    }
}
