package org.droidplanner.desktop;

public class Console {

	private static Logic logic;
	private static Map map;

	public static void main(String[] args) {
		logic = new Logic();
		map = new Map();
		map.setVisible(true);
	}

}
