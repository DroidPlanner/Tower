package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class Takeoff extends MissionItem implements MissionItem.Command {

    /**
     * Default takeoff altitude in meters.
     */
    public static final double DEFAULT_TAKEOFF_ALTITUDE = 10.0;

    private double takeoffAltitude;

    public Takeoff(){
        super(MissionItemType.TAKEOFF);
    }

    public double getTakeoffAltitude() {
        return takeoffAltitude;
    }

    public void setTakeoffAltitude(double takeoffAltitude) {
        this.takeoffAltitude = takeoffAltitude;
    }

    public static final Creator<Takeoff> CREATOR = new Creator<Takeoff>() {
        @Override
        public Takeoff createFromParcel(Parcel source) {
            return (Takeoff) source.readSerializable();
        }

        @Override
        public Takeoff[] newArray(int size) {
            return new Takeoff[size];
        }
    };
}
