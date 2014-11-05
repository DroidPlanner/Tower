package com.ox3dr.services.android.lib.gcs.follow;

/**
 * Created by fhuya on 11/5/14.
 */
public class FollowMode {

    public static final int TYPE_LEASH = 0;
    public static final int TYPE_LEAD = 1;
    public static final int TYPE_RIGHT = 2;
    public static final int TYPE_LEFT = 3;
    public static final int TYPE_CIRCLE = 4;
    public static final int TYPE_ABOVE = 5;

    private final int followType;
    private final String typeLabel;
    private final double radius;

    public FollowMode(int followType, String typeLabel, double radius) {
        this.followType = followType;
        this.typeLabel = typeLabel;
        this.radius = radius;
    }

    public int getFollowType() {
        return followType;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public double getRadius() {
        return radius;
    }
}
