package co.aerobotics.android.data;

import com.goebl.simplify.Simplify;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michaelwootton on 8/23/17.
 */

public class BoundaryDetail {

    private String boundary_id;
    private double altitude = 60;
    private double angle = 180;
    private double overlap = 70;
    private double sidelap = 70;
    private double speed = 10;
    private String name;
    private String points = "";
    private String pointAltitudes = "";
    private int clientId;
    private String camera = "";
    private Integer cropTypeId;

    private int farmId;

    public BoundaryDetail(){

    }

    public BoundaryDetail(String name, String boundary_id, String points, String pointAltitudes, int clientId, Integer cropTypeId, int farmId) {
        this.name = name;
        this.boundary_id = boundary_id;
        this.points = points;
        this.pointAltitudes = pointAltitudes;
        this.clientId = clientId;
        this.cropTypeId = cropTypeId;
        this.farmId = farmId;
    }
/*
    public BoundaryDetail(String name, int boundary_id, double altitude, double angle, double overlap, double sidelap, double speed) {
        this.boundary_id = boundary_id;
        this.name = name;
        this.altitude = altitude;
        this.angle = angle;
        this.overlap=overlap;
        this.sidelap=sidelap;
        this.speed = speed;
    }
    */

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getOverlap() {
        return overlap;
    }

    public void setOverlap(double overlap) {
        this.overlap = overlap;
    }

    public double getSidelap() {
        return sidelap;
    }

    public void setSidelap(double sidelap) {
        this.sidelap = sidelap;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getBoundaryId() {
        return boundary_id;
    }

    public void setBoundaryId(String boundary_id) {
        this.boundary_id = boundary_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getPointAltitudes() { return pointAltitudes; }

    public List<Double> getPointAltitudesAsList() {
        if (this.pointAltitudes != null && !this.pointAltitudes.equals("")) {
            String[] pointAltitudeStrings = this.pointAltitudes.split(",");
            ArrayList<Double> outputDoubles = new ArrayList<Double>();
            for (String item : pointAltitudeStrings) {
                outputDoubles.add(Double.parseDouble(item));
            }
            return outputDoubles;
        } else {
            return null;
            }
    }

    public void setPointAltitudes(String pointAltitudes) { this.pointAltitudes = pointAltitudes; }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public boolean isDisplay() {
        return true;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public CameraDetail getCameraDetailFromString(){
        if(camera!=null) {
            String valuesString = camera.split("[\\{\\}]")[1];
            String[] valuesList = valuesString.split(",");
            String[] actualValues = new String[valuesList.length];
            for (int i = 0; i < valuesList.length; i++) {
                actualValues[i] = valuesList[i].trim().split("=")[1];
            }

            return new CameraDetail(actualValues[0].split("[\\'\\']")[1],
                    Double.valueOf(actualValues[1]), Double.valueOf(actualValues[2]), Double.valueOf(actualValues[3]),
                    Double.valueOf(actualValues[4]), Double.valueOf(actualValues[5]), Double.valueOf(actualValues[6]),
                    Boolean.valueOf(actualValues[7]));
        } else {
            return null;
        }
    }

    public int getFarmId() {
        return farmId;
    }

    public void setFarmId(int farmId) {
        this.farmId = farmId;
    }

    public Integer getCropTypeId() {
        return cropTypeId;
    }

    public void setCropTypeId(Integer cropTypeId) {
        this.cropTypeId = cropTypeId;
    }
}
