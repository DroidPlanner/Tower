package org.droidplanner.android.graphic.map;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.maps.DPMap.PathSource;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.MarkerWithText;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import com.ox3dr.services.android.lib.coordinate.LatLong;

public class GraphicGuided extends MarkerInfo.SimpleMarkerInfo implements PathSource {

	private final static String TAG = GraphicGuided.class.getSimpleName();

	private GuidedPoint guidedPoint;
	private GPS gps;

	public GraphicGuided(Drone drone) {
		guidedPoint = drone.getGuidedPoint();
		gps = drone.getGps();
	}

	@Override
	public List<Coord2D> getPathPoints() {
		List<Coord2D> path = new ArrayList<Coord2D>();
		if (guidedPoint.isActive()) {
			if (gps.isPositionValid()) {
				path.add(gps.getPosition());
			}
			path.add(guidedPoint.getCoord());
		}
		return path;
	}

	@Override
	public boolean isVisible() {
		return guidedPoint.isActive();
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
	public com.ox3dr.services.android.lib.coordinate.LatLong getPosition() {
		return guidedPoint.getCoord();
	}

	@Override
	public void setPosition(LatLong coord) {
		try {
			guidedPoint.forcedGuidedCoordinate(coord);
		} catch (Exception e) {
			Log.e(TAG, "Unable to update guided point position.", e);
		}
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return MarkerWithText.getMarkerWithTextAndDetail(R.drawable.ic_wp_map, "Guided", "", res);
	}

	@Override
	public boolean isDraggable() {
		return true;
	}
}