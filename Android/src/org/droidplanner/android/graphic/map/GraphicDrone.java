package org.droidplanner.android.graphic.map;

import org.droidplanner.R;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.model.Drone;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GraphicDrone extends MarkerInfo.SimpleMarkerInfo {

	private Drone drone;
	public GraphicDrone(Drone drone) {
		this.drone = drone;
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Coord2D getPosition() {
		return drone.getGps().getPosition();
	}

	public Coord2D getInterpolatedPosition(){
		Coord2D realPosition = getPosition();
		if(drone.getMavClient().isConnected()){
			int timeDelta = drone.getGps().getPositionAgeInMillis();
			Speed groundSpeed = drone.getSpeed().getGroundSpeed();
			double course = drone.getGps().getCourse();
			return GeoTools.newCoordFromBearingAndDistance(realPosition,course,
					timeDelta/1000.0* groundSpeed.valueInMetersPerSecond());
		}else{
			return realPosition;
		}
	}

	@Override
	public Bitmap getIcon(Resources res) {
		if(drone.isConnectionAlive()) {
			return BitmapFactory.decodeResource(res, R.drawable.quad);
		}
		return BitmapFactory.decodeResource(res, R.drawable.quad_disconnect);

	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}

	@Override
	public float getRotation() {
		return (float) drone.getOrientation().getYaw();
	}
}
