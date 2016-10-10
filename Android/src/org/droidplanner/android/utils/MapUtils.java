package org.droidplanner.android.utils;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.o3dr.services.android.lib.util.MathUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapUtils {
	static public LatLng coordToLatLng(LatLong coord) {
		return new LatLng(coord.getLatitude(), coord.getLongitude());
	}

	public static List<LatLng> coordToLatLng(List<? extends LatLong> coords){
		List<LatLng> points = new ArrayList<>(coords.size());
		for(LatLong coord: coords){
			points.add(coordToLatLng(coord));
		}

		return points;
	}

    public static LatLong latLngToCoord(LatLng point) {
        return new LatLong((float)point.latitude, (float) point.longitude);
    }

	public static LatLong locationToCoord(Location location) {
		return new LatLong((float) location.getLatitude(), (float) location.getLongitude());
	}

	public static int scaleDpToPixels(double value, Resources res) {
		final float scale = res.getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}

    /**
     * Export the given path as a Mission
     * @param pathPoints
     * @return
     */
	public static List<MissionItem> exportPathAsMissionItems(List<? extends LatLongAlt> pathPoints, double toleranceInPixels) {
        List<MissionItem> exportedMissionItems = new LinkedList<>();
        if(pathPoints != null && !pathPoints.isEmpty()) {
            List<LatLong> simplifiedPath = MathUtils.simplify(pathPoints, toleranceInPixels);

            int pointsCount = simplifiedPath.size();
            if(pointsCount > 3){
                // When taking off and/or landing the altitude has a tendency to be a bit too low.
                LatLongAlt first = (LatLongAlt) simplifiedPath.get(0);
                LatLongAlt second = (LatLongAlt) simplifiedPath.get(1);
                first.setAltitude((second.getAltitude() + first.getAltitude())/ 2.0);

                LatLongAlt beforeLast = (LatLongAlt) simplifiedPath.get(pointsCount - 2);
                LatLongAlt last = (LatLongAlt) simplifiedPath.get(pointsCount -1);
                last.setAltitude((last.getAltitude() + beforeLast.getAltitude())/2.0);
            }

            SpaceTime lastPoint = null;
            for(LatLong point : simplifiedPath) {
                if(point instanceof SpaceTime) {
                    SpaceTime currentPoint = (SpaceTime) point;
                    if(lastPoint != null) {
                        // Calculate the speed used by the vehicle from the last point to the
                        // current one.
                        double distanceInM = MathUtils.getDistance3D(lastPoint, currentPoint);
                        float deltaTimeInSecs = Math.abs(currentPoint.getTimeInMs()
                            - lastPoint.getTimeInMs()) / 1000F;

                        if (Float.compare(deltaTimeInSecs, 0f) != 0) {
                            double speed = distanceInM / deltaTimeInSecs;
                            ChangeSpeed speedMissionItem = new ChangeSpeed();
                            speedMissionItem.setSpeed(speed);
                            exportedMissionItems.add(speedMissionItem);
                        }
                    }
                    lastPoint = currentPoint;
                }
                else {
                    lastPoint = null;
                }
                SplineWaypoint waypoint = new SplineWaypoint();
                waypoint.setCoordinate((LatLongAlt) point);
                exportedMissionItems.add(waypoint);
            }
        }

        return exportedMissionItems;
    }

	public static List<com.baidu.mapapi.model.LatLng> coordToBaiduLatLng(List<? extends LatLong> coords) {
        List<com.baidu.mapapi.model.LatLng> points = new ArrayList<>(coords.size());
        for(LatLong coord: coords){
            points.add(coordToBaiduLatLng(coord));
        }
        return points;
	}

	public static com.baidu.mapapi.model.LatLng coordToBaiduLatLng(LatLong coord) {
        return new com.baidu.mapapi.model.LatLng(coord.getLatitude(), coord.getLongitude());
    }

	public static LatLong baiduLatLngToCoord(com.baidu.mapapi.model.LatLng point) {
        return new LatLong((float)point.latitude, (float) point.longitude);
    }
}
