package org.droidplanner.desktop.ui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneEvents;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Length;

public class TelemetryPanel extends JPanel implements OnDroneListener {

	private static final long serialVersionUID = 5023065582658742109L;

	private static final String ALTITUDE = "Altitude: ";
	private static final String LONGITUDE = "Longitude: ";
	private static final String LATITUDE = "Latitude: ";

	private static final String SPEED = "Speed: ";

	private static final String MODE = "Mode: ";

	private JLabel labelLatitude;
	private JLabel labelLongitude;
	private JLabel labelAltitude;
	private JLabel labelSpeed;
	private JLabel labelMode;

	public TelemetryPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		labelLatitude = new JLabel(LATITUDE);
		labelLongitude = new JLabel(LONGITUDE);
		labelAltitude = new JLabel(ALTITUDE);
		labelSpeed = new JLabel(SPEED);
		labelMode = new JLabel(MODE);

		add(labelLatitude);
		add(labelLongitude);
		add(labelAltitude);
		add(labelSpeed);
		add(labelMode);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case GPS:
			Coord2D position = drone.GPS.getPosition();
			labelLatitude.setText(LATITUDE + position.getLat());
			labelLongitude.setText(LONGITUDE + position.getLng());
			break;
		case SPEED:
			labelSpeed.setText(SPEED + drone.speed.getGroundSpeed().toStringInMetersPerSecond());
			labelAltitude.setText(ALTITUDE
					+ new Length(drone.altitude.getAltitude()));
			break;
		case MODE:
			labelMode.setText(MODE + drone.state.getMode().toString());
			break;
		default:
			break;
		}
		this.repaint();
	}

	static TelemetryPanel createTelemetryPanel(DroneEvents events) {
		TelemetryPanel panel = new TelemetryPanel();
		events.addDroneListener(panel);
		return panel;
	}
}
