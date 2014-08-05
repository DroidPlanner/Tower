package org.droidplanner.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Menu extends JMenuBar implements ActionListener {
	private static final long serialVersionUID = 3070514693880578004L;

	private static final String LOAD_MISSION = "Load Mission";
	private static final String LOAD_PARAMETERS = "Load Parameters";
	
	public Menu(){
		super();
		JMenu drone = new JMenu("Drone");
		JMenuItem loadMission = new JMenuItem(LOAD_MISSION);
		JMenuItem loadParameters = new JMenuItem(LOAD_PARAMETERS);
		loadMission.addActionListener(this);
		drone.add(loadMission);
		drone.add(loadParameters);
		add(drone);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase(LOAD_MISSION)) {
			//TODO load the mission here
		}else if (e.getActionCommand().equalsIgnoreCase(LOAD_PARAMETERS)) {
			//TODO load the parameters here			
		}
	}

}
