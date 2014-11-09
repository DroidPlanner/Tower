package com.ox3dr.services.android.lib.drone.mission.item.raw;

import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/8/14.
 */
public class GlobalPositionIntMessage extends MissionItem {

    private static final int MAVLINK_MSG_ID_GLOBAL_POSITION_INT = 33;

    private  int sysid;
    private int compid;

    /**
     * Timestamp (milliseconds since system boot)
     */
    private int time_boot_ms;
    /**
     * Latitude, expressed as * 1E7
     */
    private int lat;
    /**
     * Longitude, expressed as * 1E7
     */
    private int lon;
    /**
     * Altitude in meters, expressed as * 1000 (millimeters), above MSL
     */
    private int alt;
    /**
     * Altitude above ground in meters, expressed as * 1000 (millimeters)
     */
    private int relative_alt;
    /**
     * Ground X Speed (Latitude), expressed as m/s * 100
     */
    private short vx;
    /**
     * Ground Y Speed (Longitude), expressed as m/s * 100
     */
    private short vy;
    /**
     * Ground Z Speed (Altitude), expressed as m/s * 100
     */
    private short vz;
    /**
     * Compass heading in degrees * 100, 0.0..359.99 degrees. If unknown, set to: UINT16_MAX
     */
    private short hdg;

    public GlobalPositionIntMessage(){
        super(MissionItemType.RAW_MESSAGE);
    }

    public int getMessageId(){
        return MAVLINK_MSG_ID_GLOBAL_POSITION_INT;
    }

    public int getSysid() {
        return sysid;
    }

    public void setSysid(int sysid) {
        this.sysid = sysid;
    }

    public int getCompid() {
        return compid;
    }

    public void setCompid(int compid) {
        this.compid = compid;
    }

    public int getTime_boot_ms() {
        return time_boot_ms;
    }

    public void setTime_boot_ms(int time_boot_ms) {
        this.time_boot_ms = time_boot_ms;
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public int getAlt() {
        return alt;
    }

    public void setAlt(int alt) {
        this.alt = alt;
    }

    public int getRelative_alt() {
        return relative_alt;
    }

    public void setRelative_alt(int relative_alt) {
        this.relative_alt = relative_alt;
    }

    public short getVx() {
        return vx;
    }

    public void setVx(short vx) {
        this.vx = vx;
    }

    public short getVy() {
        return vy;
    }

    public void setVy(short vy) {
        this.vy = vy;
    }

    public short getVz() {
        return vz;
    }

    public void setVz(short vz) {
        this.vz = vz;
    }

    public short getHdg() {
        return hdg;
    }

    public void setHdg(short hdg) {
        this.hdg = hdg;
    }

    @Override
    public String toString() {
        return "GlobalPositionIntMessage{" +
                "sysid=" + sysid +
                ", compid=" + compid +
                ", time_boot_ms=" + time_boot_ms +
                ", lat=" + lat +
                ", lon=" + lon +
                ", alt=" + alt +
                ", relative_alt=" + relative_alt +
                ", vx=" + vx +
                ", vy=" + vy +
                ", vz=" + vz +
                ", hdg=" + hdg +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<GlobalPositionIntMessage> CREATOR = new Parcelable
            .Creator<GlobalPositionIntMessage>() {
        public GlobalPositionIntMessage createFromParcel(Parcel source) {
            return (GlobalPositionIntMessage) source.readSerializable();
        }

        public GlobalPositionIntMessage[] newArray(int size) {
            return new GlobalPositionIntMessage[size];
        }
    };
}
