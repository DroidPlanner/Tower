package org.droidplanner.core.mission;

import org.droidplanner.core.mission.commands.ReturnToHome;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.waypoints.Land;
import org.droidplanner.core.mission.waypoints.Loiter;
import org.droidplanner.core.mission.waypoints.LoiterTime;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.core.mission.waypoints.Takeoff;
import org.droidplanner.core.mission.waypoints.Waypoint;

import java.lang.reflect.Constructor;

public enum MissionItemTypes {
    WAYPOINT("Waypoint", "", Waypoint.class),
    TAKEOFF("Takeoff", "", Takeoff.class),
    RTL("Return to Launch", "", ReturnToHome.class),
    LAND("Land", "", Land.class),
    LOITERN("Circle", "", Loiter.class),
    LOITERT("Loiter", "", LoiterTime.class),
    // LOITER("Loiter indefinitly", "", LoiterInfinite.class),
    ROI("Region of Interest", "", RegionOfInterest.class),
    SURVEY("Survey", "", Survey.class);

    private final String name;
    private final String description;
    private final Class<? extends MissionItem> coreImpl;

    private MissionItemTypes(String name, String description, Class<? extends MissionItem> impl) {
        this.name = name;
        this.description = description;
        this.coreImpl = impl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public MissionItem getNewItem(Object... constructorArgs) throws IllegalArgumentException {
        MissionItem item;
        try {
            final int argsLength = constructorArgs.length;
            if (argsLength == 0) {
                item = coreImpl.newInstance();
            } else {
                Class<?>[] argsTypes = new Class[constructorArgs.length];
                for(int i = 0; i < argsLength; i++){
                    argsTypes[i] = constructorArgs[i].getClass();
                }

                Constructor<? extends MissionItem> constructor = coreImpl.getConstructor(argsTypes);
                item = constructor.newInstance(constructorArgs);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid test fragment class.");
        }

        return item;
    }
}