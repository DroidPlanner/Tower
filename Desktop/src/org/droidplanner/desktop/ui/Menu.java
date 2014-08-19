package org.droidplanner.desktop.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.gcs.follow.Follow;

public class Menu extends JMenuBar implements ActionListener {
	private static final long serialVersionUID = 3070514693880578004L;

	private static final String LOAD_MISSION = "Load Mission";
	private static final String LOAD_PARAMETERS = "Load Parameters";
	private static final String FOLLOW = "Follow";

	private Drone drone;

	private Follow follow;

	public Menu(Drone drone, Follow follow) {
		super();
		this.drone = drone;
		this.follow = follow;
		
		JMenu droneMenu = new JMenu("Drone");
		JMenuItem loadMission = new JMenuItem(LOAD_MISSION);
		JMenuItem loadParameters = new JMenuItem(LOAD_PARAMETERS);
		JMenuItem followM = new JMenuItem(FOLLOW);

		loadMission.addActionListener(this);
		loadParameters.addActionListener(this);
		followM.addActionListener(this);

		droneMenu.add(loadMission);
		droneMenu.add(loadParameters);
		droneMenu.add(followM);
		add(droneMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase(LOAD_MISSION)) {
			drone.waypointManager.getWaypoints();
		} else if (e.getActionCommand().equalsIgnoreCase(LOAD_PARAMETERS)) {
			drone.parameters.getAllParameters();
		} else if (e.getActionCommand().equalsIgnoreCase(FOLLOW)) {
			follow.toggleFollowMeState();
		}
	}

}
