package org.droidplanner.android.graphic.managers;

import java.util.List;

import org.droidplanner.android.fragments.helpers.MapPath;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.graphic.map.GraphicMission;
import org.droidplanner.android.graphic.map.MarkerManager;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneEvents;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapManager implements OnDroneListener {
	private GoogleMap mMap;
	private Context context;
	private boolean isMissionDraggable = false;

	public MarkerManager markers;
	public MapPath missionPath;
	public GraphicHome home;
	public GraphicMission mission;
	private MapPath droneLeashPath;
	private Polyline flightPath;
	public int maxFlightPathSize;

	public GraphicDrone droneMarker;
	public GraphicGuided guided;
	private DroneEvents events;

	public MapManager(GoogleMap map, Drone drone, Resources resources,
			Context context) {
		this.mMap = map;
		this.context = context;
		home = new GraphicHome(drone);
		mission = new GraphicMission(drone);
		missionPath = new MapPath(mMap, resources);
		markers = new MarkerManager(mMap);

		// From Flight Map
		droneMarker = new GraphicDrone(drone, mMap);
		droneLeashPath = new MapPath(mMap, resources);
		guided = new GraphicGuided(drone);

		addFlightPathToMap();
	}

	public void stopListeningToDrone() {
		events.removeDroneListener(this);
	}

	public void update() {
		markers.clean();

		if (home.isValid()) {
			markers.updateMarker(home, false, context);
		}

		markers.updateMarkers(mission.getMarkers(), isMissionDraggable, context);

		// TODO reimplement the mission path
		// missionPath.update(mission);

		// Moved from EditorMap
		// markers.updateMarkers(polygon .getPolygonPoints(), true, context);
		// polygonPath.update(polygon);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MISSION_UPDATE:
			update();
			break;
		default:
			break;
		}

		// From Flight Activity
		LatLng position = DroneHelper.CoordToLatLang(drone.GPS.getPosition());
		switch (event) {
		case GPS:
			droneLeashPath.update(guided);
			addFlightPathPoint(position);
			break;
		case GUIDEDPOINT:
			markers.updateMarker(guided, true, context);
			droneLeashPath.update(guided);
			break;
		default:
			break;
		}
	}

	public void addFlightPathPoint(LatLng position) {
		if (maxFlightPathSize > 0) {
			List<LatLng> oldFlightPath = flightPath.getPoints();
			if (oldFlightPath.size() > maxFlightPathSize) {
				oldFlightPath.remove(0);
			}
			oldFlightPath.add(position);
			flightPath.setPoints(oldFlightPath);
		}
	}

	public void clearFlightPath() {
		List<LatLng> oldFlightPath = flightPath.getPoints();
		oldFlightPath.clear();
		flightPath.setPoints(oldFlightPath);
	}

	private void addFlightPathToMap() {
		PolylineOptions flightPathOptions = new PolylineOptions();
		flightPathOptions.color(0xfffd693f).width(6).zIndex(1);
		flightPath = mMap.addPolyline(flightPathOptions);
	}

	public void setMissionDraggable(boolean isDraggable) {
		isMissionDraggable = isDraggable;
	}
}