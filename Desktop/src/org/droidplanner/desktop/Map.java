package org.droidplanner.desktop;

import javax.swing.JFrame;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class Map extends JFrame {
	private static final long serialVersionUID = 1L;

	
	public Map(){
		super("Map");
		setSize(800, 600);
		JMapViewer map = new JMapViewer();
		add(map);
		
		map.addMapMarker(new MapMarkerDot(-29, -51));
	}
}
