package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class Circle extends BaseSpatialItem {

    private double radius = 10;
    private int turns = 1;

    public Circle() {
        super(MissionItemType.CIRCLE);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public static final Creator<Circle> CREATOR = new Creator<Circle>() {
        @Override
        public Circle createFromParcel(Parcel source) {
            return (Circle) source.readSerializable();
        }

        @Override
        public Circle[] newArray(int size) {
            return new Circle[size];
        }
    };
}
