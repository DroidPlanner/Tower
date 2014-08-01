package org.droidplanner.desktop;

import java.io.IOException;

import javax.swing.JFrame;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;

public class Map extends JFrame {
	private static final long serialVersionUID = 1L;

	
	public Map(){
		super("Map");
		setSize(800, 600);
		JMapViewer map = new JMapViewer();
		try {
			map.setTileLoader(new OsmFileCacheTileLoader(map));
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		add(map);
		
		map.addMapMarker(new MapMarkerDot(-29, -51));
	}
}
