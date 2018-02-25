package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by fhuya on 11/6/14.
 */
public class Circle extends BaseSpatialItem implements android.os.Parcelable {

    private double radius = 10;
    private int turns = 1;

    public Circle() {
        super(MissionItemType.CIRCLE);
    }

    public Circle(Circle copy){
        super(copy);
        this.radius = copy.radius;
        this.turns = copy.turns;
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

    @Override
    public String toString() {
        return "Circle{" +
                "radius=" + radius +
                ", turns=" + turns +
                ", " + super.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Circle)) return false;
        if (!super.equals(o)) return false;

        Circle circle = (Circle) o;

        if (Double.compare(circle.radius, radius) != 0) return false;
        return turns == circle.turns;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + turns;
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.radius);
        dest.writeInt(this.turns);
    }

    private Circle(Parcel in) {
        super(in);
        this.radius = in.readDouble();
        this.turns = in.readInt();
    }

    @Override
    public MissionItem clone() {
        return new Circle(this);
    }

    public static final Creator<Circle> CREATOR = new Creator<Circle>() {
        public Circle createFromParcel(Parcel source) {
            return new Circle(source);
        }

        public Circle[] newArray(int size) {
            return new Circle[size];
        }
    };
}
