package com.three_dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a set of mavlink mission item messages.
 */
public class Mission implements Parcelable {

    private final List<MissionItemMessage> missionItemsList = new ArrayList<MissionItemMessage>();

    public void addMissionItemMessage(MissionItemMessage missionItem){
        missionItemsList.add(missionItem);
    }

    public void removeMissionItemMessage(MissionItemMessage missionItem){
        missionItemsList.remove(missionItem);
    }

    public void clear(){
        missionItemsList.clear();
    }

    public List<MissionItemMessage> getMissionItemMessages(){
        return missionItemsList;
    }

    @Override
    public String toString() {
        return "Mission{" +
                "missionItemsList=" + missionItemsList +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(missionItemsList);
    }

    public Mission() {
    }

    private Mission(Parcel in) {
        in.readTypedList(missionItemsList, MissionItemMessage.CREATOR);
    }

    public static final Parcelable.Creator<Mission> CREATOR = new Parcelable.Creator<Mission>() {
        public Mission createFromParcel(Parcel source) {
            return new Mission(source);
        }

        public Mission[] newArray(int size) {
            return new Mission[size];
        }
    };
}
