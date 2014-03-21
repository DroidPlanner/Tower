package org.droidplanner.graphic;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.drone.Drone;
import org.droidplanner.fragments.helpers.MapPath.PathSource;
import org.droidplanner.graphic.markers.MarkerManager.MarkerSource;
import org.droidplanner.mission.Mission;
import org.droidplanner.mission.MissionItem;

import com.google.android.gms.maps.model.LatLng;

public class GraphicMission implements PathSource {

	Mission mission;

	public GraphicMission(Drone drone) {
		this.mission = drone.mission;
	}

	public List<MarkerSource> getMarkers() {
		List<MarkerSource> markers = new ArrayList<MarkerSource>();
		for (MissionItem item : mission.getItems()) {
			if (item instanceof MarkerSource) {
				markers.add((MarkerSource) item);
			}
		}
		return markers;
	}

	@Override
	public List<LatLng> getPathPoints() {
		// TODO Auto-generated method stub
		return null;
	}

}
