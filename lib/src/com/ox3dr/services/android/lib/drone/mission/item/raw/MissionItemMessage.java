package com.ox3dr.services.android.lib.drone.mission.item.raw;

import android.os.Parcel;
import android.os.Parcelable;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Message encoding a mission item. This message is emitted to announce
 * the presence of a mission item and to set a mission item on the system.
 */
public class MissionItemMessage extends MissionItem {

    private static final int MAVLINK_MSG_ID_MISSION_ITEM = 39;

    private int sysId;
    private int compId;

    /**
     * PARAM1, see MAV_CMD enum
     */
    private float param1;
    /**
     * PARAM2, see MAV_CMD enum
     */
    private float param2;
    /**
     * PARAM3, see MAV_CMD enum
     */
    private float param3;
    /**
     * PARAM4, see MAV_CMD enum
     */
    private float param4;
    /**
     * PARAM5 / local: x position, global: latitude
     */
    private float x;
    /**
     * PARAM6 / y position: global: longitude
     */
    private float y;
    /**
     * PARAM7 / z position: global: altitude (relative or absolute, depending on frame.
     */
    private float z;
    /**
     * Sequence
     */
    private int seq;
    /**
     * The scheduled action for the MISSION. see MAV_CMD in common.xml MAVLink specs
     */
    private int command;
    /**
     * System ID
     */
    private byte target_system;
    /**
     * Component ID
     */
    private byte target_component;
    /**
     * The coordinate system of the MISSION. see MAV_FRAME in mavlink_types.h
     */
    private byte frame;
    /**
     * false:0, true:1
     */
    private byte current;
    /**
     * autocontinue to next wp
     */
    private byte autocontinue;

    public MissionItemMessage() {
        super(MissionItemType.RAW_MESSAGE);
    }

    public int getMessageId(){
        return MAVLINK_MSG_ID_MISSION_ITEM;
    }

    public int getSysId() {
        return sysId;
    }

    public void setSysId(int sysId) {
        this.sysId = sysId;
    }

    public int getCompId() {
        return compId;
    }

    public void setCompId(int compId) {
        this.compId = compId;
    }

    public float getParam1() {
        return param1;
    }

    public void setParam1(float param1) {
        this.param1 = param1;
    }

    public float getParam2() {
        return param2;
    }

    public void setParam2(float param2) {
        this.param2 = param2;
    }

    public float getParam3() {
        return param3;
    }

    public void setParam3(float param3) {
        this.param3 = param3;
    }

    public float getParam4() {
        return param4;
    }

    public void setParam4(float param4) {
        this.param4 = param4;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public byte getTarget_system() {
        return target_system;
    }

    public void setTarget_system(byte target_system) {
        this.target_system = target_system;
    }

    public byte getTarget_component() {
        return target_component;
    }

    public void setTarget_component(byte target_component) {
        this.target_component = target_component;
    }

    public byte getFrame() {
        return frame;
    }

    public void setFrame(byte frame) {
        this.frame = frame;
    }

    public byte getCurrent() {
        return current;
    }

    public void setCurrent(byte current) {
        this.current = current;
    }

    public byte getAutocontinue() {
        return autocontinue;
    }

    public void setAutocontinue(byte autocontinue) {
        this.autocontinue = autocontinue;
    }

    @Override
    public String toString() {
        return "MissionItemMessage{" +
                "sysId=" + sysId +
                ", compId=" + compId +
                ", param1=" + param1 +
                ", param2=" + param2 +
                ", param3=" + param3 +
                ", param4=" + param4 +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", seq=" + seq +
                ", command=" + command +
                ", target_system=" + target_system +
                ", target_component=" + target_component +
                ", frame=" + frame +
                ", current=" + current +
                ", autocontinue=" + autocontinue +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MissionItemMessage> CREATOR = new Parcelable.Creator<MissionItemMessage>() {
        public MissionItemMessage createFromParcel(Parcel source) {
            return (MissionItemMessage) source.readSerializable();
        }

        public MissionItemMessage[] newArray(int size) {
            return new MissionItemMessage[size];
        }
    };
}

