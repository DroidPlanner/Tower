package org.droidplanner.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

import javax.swing.JFrame;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Map extends JFrame implements OnDroneListener {
	private static final long serialVersionUID = 1L;
	private MapMarkerDot marker;
	private JMapViewer map;
	private TelemetryPanel telemetryData;

	public Map() {
		super("Map");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		map = new JMapViewer();
		try {
			map.setTileLoader(new OsmFileCacheTileLoader(map));
			map.setTileSource(new OsmTileSource.CycleMap());
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		marker = new MapMarkerDot(-29, -51);
		map.addMapMarker(marker);

		telemetryData = new TelemetryPanel();

		add(telemetryData, BorderLayout.WEST);
		add(map);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		telemetryData.onDroneEvent(event, drone);

		Coord2D position = drone.GPS.getPosition();
		switch (event) {
		case GPS:
			marker.setLat(position.getLat());
			marker.setLon(position.getLng());
			map.repaint();
			break;
		case HEARTBEAT_FIRST:
			map.setDisplayPosition(
					new Coordinate(position.getLat(), position.getLng()), 17);
			break;

		case MISSION_RECEIVED:
			for (MissionItem item : drone.mission.getItems()) {
				if (item instanceof SpatialCoordItem) {
					Coord3D coordinate = ((SpatialCoordItem) item)
							.getCoordinate();
					MapMarkerDot missionMarker = new MapMarkerDot(
							coordinate.getLat(), coordinate.getLng());
					missionMarker.setBackColor(Color.BLACK);
					map.addMapMarker(missionMarker);
				}
			}
			break;
		case PARAMETERS_DOWNLOADED:
			new ParametersDialog(drone.parameters.parameterList);
			break;
		default:
			break;
		}
	}

}
