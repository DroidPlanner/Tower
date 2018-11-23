package co.aerobotics.android.mission;

/**
 * Created by michaelwootton on 2/10/18.
 */

public class MissionDetails {
    private String waypoints;
    private float imageDistance;
    private float altitude;
    private float speed;
    private String waypointAltitudes;

    public MissionDetails(){

    }
    public MissionDetails(String waypoints, float altitude, float imageDistance, float speed, String waypointAltitudes) {
        this.waypoints = waypoints;
        this.altitude = altitude;
        this.imageDistance = imageDistance;
        this.speed = speed;
        this.waypointAltitudes = waypointAltitudes;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }

    public float getImageDistance() {
        return imageDistance;
    }

    public void setImageDistance(float imageDistance) {
        this.imageDistance = imageDistance;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getWaypointAltitudes() { return waypointAltitudes; }

    public void setWaypointAltitudes(String waypointAltitudes) { this.waypointAltitudes = waypointAltitudes; }
}
