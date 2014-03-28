package org.droidplanner.android.graphic.map;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.fragments.helpers.MapPath.PathSource;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.waypoints.Waypoint;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GraphicMission implements PathSource {

	Mission mission;

	public GraphicMission(Drone drone) {
		this.mission = drone.mission;
	}

	public List<MarkerSource> getMarkers() {
		List<MarkerSource> markers = new ArrayList<MarkerSource>();
		for (MissionItem item : mission.getItems()) {
				markers.add(new itemMarker((Waypoint) item));
		}
		return markers;
	}

	@Override
	public List<LatLng> getPathPoints() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class itemMarker implements MarkerSource{
		Waypoint item;

		public itemMarker(Waypoint item) {
			this.item = item;
		}

		@Override
		public MarkerOptions build(Context context) {
			return new MarkerOptions()
					.position(DroneHelper.CoordToLatLang(item.getCoordinate()))
					.draggable(true).anchor(0.5f, 0.5f);
		}

		@Override
		public void update(Marker marker, Context context) {
			marker.setPosition(DroneHelper.CoordToLatLang(item.getCoordinate()));			
		}
	}

}
