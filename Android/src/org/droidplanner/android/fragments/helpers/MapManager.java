package org.droidplanner.android.fragments.helpers;

import org.droidplanner.android.graphic.GraphicHome;
import org.droidplanner.android.graphic.GraphicMission;
import org.droidplanner.android.graphic.markers.MarkerManager;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.maps.GoogleMap;

public class MapManager implements OnDroneListener {
	private GoogleMap mMap;
	private Context context;
	private boolean isMissionDraggable;
	
	public MarkerManager markers;
	public MapPath missionPath;
	public GraphicHome home;
	public GraphicMission mission;

	public MapManager(GoogleMap map, Drone drone, Resources resources, Context context, boolean draggable) {
		this.mMap = map;
		this.context = context;
		isMissionDraggable = draggable;		
		home = new GraphicHome(drone);
		mission = new GraphicMission(drone);
		missionPath = new MapPath(mMap,resources);
		markers = new MarkerManager(mMap);
	}


	public void update() {
		markers.clean();
	
		if (home.isValid()) {
			markers.updateMarker(home, false, context);
		}
	
		markers.updateMarkers(mission.getMarkers(), isMissionDraggable, context);
	
		//TODO reimplement the mission path
		//missionPath.update(mission);
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
	}
}