package org.droidplanner.desktop;

import org.droidplanner.desktop.logic.Logic;
import org.droidplanner.desktop.ui.Map;
import org.droidplanner.desktop.ui.Menu;

public class Application {

	private static Logic logic;
	private static Map map;

	public static void main(String[] args) {
		logic = new Logic();
		map = new Map();
		map.setVisible(true);
		map.setJMenuBar(new Menu(logic.drone));

		new Thread(logic).start();

		logic.drone.events.addDroneListener(map);
	}

}
