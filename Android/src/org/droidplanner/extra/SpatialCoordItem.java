package org.droidplanner.extra;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.SpatialCoordItemD;
import org.droidplanner.fragments.markers.GenericMarker;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.markers.helpers.MarkerWithText;
import org.droidplanner.helpers.units.Altitude;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Generic Mission item with Spatial Coordinates
 *
 */
public abstract class SpatialCoordItem extends SpatialCoordItemD implements
		MarkerSource {

	protected abstract int getIconDrawable();
	protected abstract int getIconDrawableSelected();

	@Override
	public MarkerOptions build(Context context) {
		return GenericMarker.build(coordinate).icon(getIcon(context));
	}

	@Override
	public void update(Marker marker, Context context) {
		marker.setPosition(coordinate);
		marker.setIcon(getIcon(context));
	}

	@Override
	public List<MarkerSource> getMarkers() throws Exception {
		ArrayList<MarkerSource> marker = new ArrayList<MarkerSource>();
		marker.add(this);
		return marker;
	}

	@Override
	public List<LatLng> getPath() throws Exception {
		ArrayList<LatLng> points = new ArrayList<LatLng>();
		points.add(coordinate);
		return points;
	}

	protected BitmapDescriptor getIcon(Context context) {
		int drawable;
		if (mission.selectionContains(this)) {
			drawable = getIconDrawableSelected();
		} else {
			drawable = getIconDrawable();
		}
		Bitmap marker = MarkerWithText.getMarkerWithTextAndDetail(drawable, getIconText(),
						getIconDetail(), context);
		return BitmapDescriptorFactory.fromBitmap(marker);
	}

	private String getIconDetail() {
		try {
			if (mission.getAltitudeDiffFromPreviusItem(this).valueInMeters()==0) {
				return null;
			}else{
				return null; //altitude.toString();
			}
		} catch (Exception e) {
			return null;
		}
	}

	private String getIconText() {
		return Integer.toString(mission.getNumber(this));
	}
}