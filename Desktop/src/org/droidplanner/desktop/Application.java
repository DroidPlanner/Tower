package org.droidplanner.desktop;

import org.droidplanner.desktop.logic.Logic;
import org.droidplanner.desktop.ui.UI;
import org.droidplanner.desktop.ui.Menu;

public class Application {

	private static Logic logic;
	private static UI map;

	public static void main(String[] args) {
		logic = new Logic();
		map = new UI(logic.drone.events);
		map.setVisible(true);
		map.setJMenuBar(new Menu(logic.drone,logic.follow));

		new Thread(logic).start();
	}

}
