package org.droidplanner.android.graphic.map;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.maps.DPMap.PathSource;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.MarkerWithText;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import com.o3dr.android.client.Drone;
import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.GuidedState;

public class GraphicGuided extends MarkerInfo.SimpleMarkerInfo implements PathSource {

	private final static String TAG = GraphicGuided.class.getSimpleName();

    private final Drone drone;

	public GraphicGuided(Drone drone) {
        this.drone = drone;
	}

	@Override
	public List<LatLong> getPathPoints() {
		List<LatLong> path = new ArrayList<LatLong>();
        GuidedState guidedPoint = drone.getGuidedState();
		if (guidedPoint != null && guidedPoint.isActive()) {
            Gps gps = drone.getGps();
			if (gps != null && gps.isValid()) {
				path.add(gps.getPosition());
			}
			path.add(guidedPoint.getCoordinate());
		}
		return path;
	}

	@Override
	public boolean isVisible() {
        GuidedState guidedPoint = drone.getGuidedState();
		return guidedPoint != null && guidedPoint.isActive();
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
        GuidedState guidedPoint = drone.getGuidedState();
		return guidedPoint == null ? null : guidedPoint.getCoordinate();
	}

	@Override
	public void setPosition(LatLong coord) {
		try {
			drone.sendGuidedPoint(coord, true);
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