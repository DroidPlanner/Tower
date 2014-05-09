package org.droidplanner.core.mission;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.commands.ReturnToHome;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.waypoints.Land;
import org.droidplanner.core.mission.waypoints.Loiter;
import org.droidplanner.core.mission.waypoints.LoiterInfinite;
import org.droidplanner.core.mission.waypoints.LoiterTime;
import org.droidplanner.core.mission.waypoints.LoiterTurns;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.core.mission.waypoints.Waypoint;

import java.util.Collections;

public enum MissionItemType {
    WAYPOINT("Waypoint"),
    TAKEOFF("Takeoff"),
    RTL("Return to Launch"),
    LAND("Land"),
    LOITER("Loiter"),
    LOITERN("Circle"),
    LOITERT("Loiter Time"),
    LOITER_INF("Loiter indefinitly"),
    ROI("Region of Interest"),
    SURVEY("Survey");

    private final String name;

    private MissionItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public MissionItem getNewItem(MissionItem referenceItem) throws IllegalArgumentException {
        switch (this) {
            case WAYPOINT:
                return new Waypoint(referenceItem);
            case TAKEOFF:
                return new Takeoff(referenceItem);
            case RTL:
                return new ReturnToHome(referenceItem);
            case LAND:
                return new Land(referenceItem);
            case LOITER:
                return new Loiter(referenceItem);
            case LOITERN:
                return new LoiterTurns(referenceItem);
            case LOITERT:
                return new LoiterTime(referenceItem);
            case LOITER_INF:
                return new LoiterInfinite(referenceItem);
            case ROI:
                return new RegionOfInterest(referenceItem);
            case SURVEY:
                return new Survey(referenceItem.getMission(), Collections.<Coord2D>emptyList());
            default:
                throw new IllegalArgumentException("Unrecognized mission item type (" + name + ")" +
                        ".");
        }
    }
}