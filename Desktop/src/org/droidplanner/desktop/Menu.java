package org.droidplanner.desktop;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Menu extends JMenuBar {
	private static final long serialVersionUID = 3070514693880578004L;
	
	public Menu(){
		super();
		JMenu drone = new JMenu("Drone");
		JMenuItem loadMission = new JMenuItem("Load Mission");
		JMenuItem loadParameters = new JMenuItem("Load Parameters");
		drone.add(loadMission);
		drone.add(loadParameters);
		add(drone);
	}

}
