package org.droidplanner.desktop.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneEvents;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

public class UI extends JFrame implements OnDroneListener {
	private static final long serialVersionUID = 1L;
	private TelemetryPanel telemetryData;

	public UI(DroneEvents events) {
		super("Map");
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());		
		
		telemetryData = TelemetryPanel.createTelemetryPanel(events);
		telemetryData.setPreferredSize(new Dimension(200, 0));
		add(telemetryData, BorderLayout.WEST);

		Map map = Map.createMap(events);
		add(map.map);

		events.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case PARAMETERS_DOWNLOADED:
			new ParametersDialog(drone.parameters.parameterList);
			break;
		default:
			break;
		}
	}

}
