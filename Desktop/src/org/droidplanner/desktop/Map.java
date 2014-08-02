package org.droidplanner.desktop;

import java.io.IOException;

import javax.swing.JFrame;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;

public class Map extends JFrame {
	private static final long serialVersionUID = 1L;
	private MapMarkerDot marker;
	private JMapViewer map;

	public Map() {
		super("Map");
		setSize(800, 600);
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



}
