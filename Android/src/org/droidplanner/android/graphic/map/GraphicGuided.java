package org.droidplanner.android.graphic.map;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.android.fragments.helpers.MapPath.PathSource;
import org.droidplanner.android.fragments.markers.helpers.MarkerWithText;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GraphicGuided implements MarkerSource, PathSource {

	private GuidedPoint guidedPoint;
	private GPS GPS;

	public GraphicGuided(Drone drone) {
		guidedPoint = drone.guidedPoint;
		GPS = drone.GPS;
	}

	@Override
	public MarkerOptions build(Context context) {
		return new MarkerOptions()
				.position(DroneHelper.CoordToLatLang(guidedPoint.getCoord()))
				.icon(getIcon(context)).anchor(0.5f, 0.5f).visible(false);
	}

	@Override
	public void update(Marker marker, Context context) {

		if (guidedPoint.isActive()) {
			marker.setPosition(DroneHelper.CoordToLatLang(guidedPoint
					.getCoord()));
			marker.setIcon(getIcon(context));
			marker.setVisible(true);
		} else {
			marker.setVisible(false);
		}
	}

	private static BitmapDescriptor getIcon(Context context) {
		return BitmapDescriptorFactory.fromBitmap(MarkerWithText
				.getMarkerWithTextAndDetail(R.drawable.ic_wp_map, "Guided", "",
						context));
	}

	@Override
	public List<LatLng> getPathPoints() {
		List<LatLng> path = new ArrayList<LatLng>();
		if (guidedPoint.isActive()) {
			path.add(DroneHelper.CoordToLatLang(GPS.getPosition()));
			path.add(DroneHelper.CoordToLatLang(guidedPoint.getCoord()));
		}
		return path;
	}
}