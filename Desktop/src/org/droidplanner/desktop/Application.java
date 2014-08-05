package org.droidplanner.desktop;

public class Application {

	private static Logic logic;
	private static Map map;

	public static void main(String[] args) {
		logic = new Logic();
		map = new Map();
		map.setVisible(true);
		map.setJMenuBar(new Menu());

		new Thread(logic).start();

		logic.drone.events.addDroneListener(map);
	}

}
