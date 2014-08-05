package org.droidplanner.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.droidplanner.core.drone.Drone;

public class Menu extends JMenuBar implements ActionListener {
	private static final long serialVersionUID = 3070514693880578004L;

	private static final String LOAD_MISSION = "Load Mission";
	private static final String LOAD_PARAMETERS = "Load Parameters";

	private Drone drone;
	
	public Menu(Drone drone){
		super();
		this.drone = drone;
		JMenu droneMenu = new JMenu("Drone");
		JMenuItem loadMission = new JMenuItem(LOAD_MISSION);
		JMenuItem loadParameters = new JMenuItem(LOAD_PARAMETERS);
		
		loadMission.addActionListener(this);
		loadParameters.addActionListener(this);
		
		droneMenu.add(loadMission);
		droneMenu.add(loadParameters);
		add(droneMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase(LOAD_MISSION)) {
			drone.waypointManager.getWaypoints();
		}else if (e.getActionCommand().equalsIgnoreCase(LOAD_PARAMETERS)) {
			drone.parameters.getAllParameters();
		}
	}

}
