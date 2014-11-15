package com.o3dr.services.android.lib.drone.mission;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a set of mavlink mission item messages.
 */
public class Mission implements Parcelable, Serializable {

    private short currentMissionItem;
    private final List<MissionItem> missionItemsList = new ArrayList<MissionItem>();

    public void addMissionItem(MissionItem missionItem){
        missionItemsList.add(missionItem);
    }

    public void addMissionItem(int index, MissionItem missionItem){
        missionItemsList.add(index, missionItem);
    }

    public void removeMissionItem(MissionItem missionItem){
        missionItemsList.remove(missionItem);
    }

    public void removeMissionItem(int index){
        missionItemsList.remove(index);
    }

    public void clear(){
        missionItemsList.clear();
    }

    public MissionItem getMissionItem(int index){
        return missionItemsList.get(index);
    }

    public List<MissionItem> getMissionItems(){
        return missionItemsList;
    }

    public short getCurrentMissionItem() {
        return currentMissionItem;
    }

    public void setCurrentMissionItem(short currentMissionItem) {
        this.currentMissionItem = currentMissionItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Parcelable.Creator<Mission> CREATOR = new Parcelable.Creator<Mission>() {
        public Mission createFromParcel(Parcel source) {
            return (Mission) source.readSerializable();
        }

        public Mission[] newArray(int size) {
            return new Mission[size];
        }
    };
}
