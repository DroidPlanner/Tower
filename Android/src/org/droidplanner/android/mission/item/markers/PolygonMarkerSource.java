package org.droidplanner.android.mission.item.markers;

import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PolygonMarkerSource implements MarkerSource {
	
	private Coord2D point;

	public PolygonMarkerSource(Coord2D point) {
		this.point = point;
	}

	@Override
	public MarkerOptions build(Context context) {
        return new MarkerOptions().position(DroneHelper.CoordToLatLang(point)).draggable(false).anchor(0.5f, 0.5f);
	}

	@Override
	public void update(Marker marker, Context context) {			
	}

}