package org.droidplanner.desktop;

import org.droidplanner.desktop.logic.Logic;
import org.droidplanner.desktop.ui.Menu;
import org.droidplanner.desktop.ui.UI;

public class Application {

	private static Logic logic;
	private static UI ui;

	public static void main(String[] args) {
		logic = new Logic();
		ui = new UI(logic.drone);
		ui.setJMenuBar(new Menu(logic));
		ui.setVisible(true);

		new Thread(logic).start();
	}

}
