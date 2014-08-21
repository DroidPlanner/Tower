package org.droidplanner.desktop.ui;

import java.awt.Color;
import java.io.IOException;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneEvents;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapMarkerIcon;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Map implements OnDroneListener {
	public MapMarkerIcon marker;
	public JMapViewer map;
	public MapMarkerDot guidedMarker;

	public Map() {
		map = new JMapViewer();
		try {
			map.setTileLoader(new OsmFileCacheTileLoader(map));
			map.setTileSource(new OsmTileSource.CycleMap());
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		marker = new MapMarkerIcon(new Coordinate(-29, -51));
		map.addMapMarker(marker);

		guidedMarker = new MapMarkerDot(new Coordinate(0, 0));
		guidedMarker.setVisible(false);
		map.addMapMarker(guidedMarker);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		Coord2D position = drone.GPS.getPosition();
		switch (event) {
		case GPS:
			marker.setLat(position.getLat());
			marker.setLon(position.getLng());
			marker.setRotation(drone.orientation.getYaw());
			map.repaint();
			break;
		case HEARTBEAT_FIRST:
			map.setDisplayPosition(new Coordinate(position.getLat(), position.getLng()), 17);
			break;
		case MISSION_RECEIVED:
			for (MissionItem item : drone.mission.getItems()) {
				if (item instanceof SpatialCoordItem) {
					Coord3D coordinate = ((SpatialCoordItem) item).getCoordinate();
					MapMarkerDot missionMarker = new MapMarkerDot(coordinate.getLat(),
							coordinate.getLng());
					missionMarker.setBackColor(Color.BLACK);
					map.addMapMarker(missionMarker);
				}
			}
			break;
		case GUIDEDPOINT:
			guidedMarker.setVisible(true);
			guidedMarker.setLat(drone.guidedPoint.getCoord().getLat());
			guidedMarker.setLon(drone.guidedPoint.getCoord().getLng());
			map.repaint();
			break;
		default:
			break;
		}	
	}
	
	static Map createMap(DroneEvents events) {
		Map mMap = new Map();
		events.addDroneListener(mMap);
		return mMap;
	}

}