package org.droidplanner.desktop;

import java.io.IOException;

import javax.swing.JFrame;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class Map extends JFrame implements OnDroneListener {
	private static final long serialVersionUID = 1L;
	private MapMarkerDot marker;
	private JMapViewer map;

	public Map() {
		super("Map");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		map = new JMapViewer();
		try {
			map.setTileLoader(new OsmFileCacheTileLoader(map));
			map.setTileSource(new OsmTileSource.CycleMap());
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		add(map);
		marker = new MapMarkerDot(-29, -51);
		map.addMapMarker(marker);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		System.out.println(event);

		switch (event) {
		case GPS:
			Coord2D position = drone.GPS.getPosition();
			marker.setLat(position.getLat());
			marker.setLon(position.getLng());
			map.repaint();
			break;
		default:
			break;
		}
	}

}
