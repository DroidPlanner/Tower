package org.droidplanner.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

public class UI extends JFrame implements OnDroneListener {
	private static final long serialVersionUID = 1L;
	private TelemetryPanel telemetryData;

	public UI(org.droidplanner.core.model.Drone drone) {
		super("Map");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		telemetryData = TelemetryPanel.createTelemetryPanel(drone);
		telemetryData.setPreferredSize(new Dimension(200, 0));
		add(telemetryData, BorderLayout.WEST);

		Map map = Map.createMap(drone);
		add(map.map);

		drone.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case PARAMETERS_DOWNLOADED:
			new ParametersDialog(drone.getParameters().parameterList);
			break;
		default:
			break;
		}
	}

}
