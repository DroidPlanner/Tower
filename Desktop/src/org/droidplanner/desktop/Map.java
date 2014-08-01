package org.droidplanner.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;

public class Map extends JFrame implements MouseListener {
	private static final long serialVersionUID = 1L;
	private MapMarkerDot marker;
	private JMapViewer map;

	
	public Map(){
		super("Map");
		setSize(800, 600);
		map = new JMapViewer();
		try {
			map.setTileLoader(new OsmFileCacheTileLoader(map));
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		add(map);
		map.addMouseListener(this);
		marker = new MapMarkerDot(-29, -51);
		map.addMapMarker(marker);
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		System.out.println("mouse");
		marker.setLon(marker.getLon()+1);
		map.repaint();
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
