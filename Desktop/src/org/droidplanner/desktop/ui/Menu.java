package org.droidplanner.desktop.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.desktop.logic.Logic;

public class Menu extends JMenuBar implements ActionListener {
	private static final long serialVersionUID = 3070514693880578004L;

	public enum DroneMenu {
		LOAD_MISSION("Load Mission"), LOAD_PARAMETERS("Load Parameters"), FOLLOW("Follow"), GRAPH(
				"Graph");
		private String text;

		private DroneMenu(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	private Drone drone;
	private Follow follow;

	public Menu(Logic logic) {
		super();
		this.drone = logic.drone;
		this.follow = logic.follow;

		JMenu droneMenu = new JMenu("Drone");

		for (DroneMenu menu : DroneMenu.values()) {
			JMenuItem jMenu = new JMenuItem(menu.toString());
			jMenu.addActionListener(this);
			droneMenu.add(jMenu);
		}
		add(droneMenu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase(DroneMenu.LOAD_MISSION.toString())) {
			drone.waypointManager.getWaypoints();
		} else if (e.getActionCommand().equalsIgnoreCase(DroneMenu.LOAD_PARAMETERS.toString())) {
			drone.parameters.getAllParameters();
		} else if (e.getActionCommand().equalsIgnoreCase(DroneMenu.FOLLOW.toString())) {
			follow.toggleFollowMeState();
		} else if (e.getActionCommand().equalsIgnoreCase(DroneMenu.GRAPH.toString())) {
			Graph.createGraph(drone.events);
		}
	}

}
