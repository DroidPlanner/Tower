package org.droidplanner.desktop.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.droidplanner.core.MAVLink.MAVLinkStreams.MAVLinkOutputStream;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Type.FirmwareType;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.Follow.TextNotificationReceiver;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.desktop.Communication.Connection;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

public class Logic implements Runnable {
	public Drone drone;
	private MavLinkMsgHandler mavlinkHandler;
	protected Connection link = new Connection(14550);
	public Follow follow;

	public Logic() {
		setup();
	}

	private void setup() {
		drone = droneFactory();
		mavlinkHandler = new MavLinkMsgHandler(drone);

		follow = new Follow(drone, new Handler() {

			@Override
			public void removeCallbacks(Runnable thread) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				// TODO Auto-generated method stub

			}
		}, new LocationFinder() {

			private LocationReceiver receiver;

			@Override
			public void setLocationListner(LocationReceiver receiver) {
				this.receiver = receiver;				
			}

			@Override
			public void enableLocationUpdates() {
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					Coord2D start = new Coord2D(-35.363154,149.165067);
					Location[] locations = {
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 00)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 10)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 20)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 30)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 40)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 50)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 60)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 70)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 80)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 90)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 80)), 
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 70)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 60)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 50)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 40)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 30)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 20)),
							new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 10)),
							};
					int index = 0;

					@Override
					public void run() {
						receiver.onLocationChanged(locations[index++]);
						if (index >= locations.length) {
							index = 0;
						}

					}
				}, 0, 5*1000);

			}

			@Override
			public void disableLocationUpdates() {
				// TODO Auto-generated method stub

			}
		}, new TextNotificationReceiver() {

			@Override
			public void shortText(String notification) {
				System.out.println(notification);

			}
		});

	}

	private Drone droneFactory() {
		MAVLinkOutputStream MAVClient = new MAVLinkOutputStream() {

			@Override
			public void toggleConnectionState() {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendMavPacket(MAVLinkPacket packet) {
				link.sendBuffer(packet.encodePacket());
			}

			@Override
			public void queryConnectionState() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isConnected() {
				// TODO Auto-generated method stub
				return true;
			}
		};
		Clock clock = new Clock() {

			@Override
			public long elapsedRealtime() {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		Preferences pref = new Preferences() {

			@Override
			public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public FirmwareType getVehicleType() {
				// TODO Auto-generated method stub
				return FirmwareType.ARDU_COPTER;
			}

			@Override
			public Rates getRates() {
				return new Rates();
			}
		};
		Handler handler = new Handler() {

			@Override
			public void removeCallbacks(Runnable thread) {
				// TODO Auto-generated method stub

			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				// TODO Auto-generated method stub

			}
		};

		return new Drone(MAVClient, clock, handler, pref);
	}

	@Override
	public void run() {
		try {
			link.getUdpStream();
			System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost()
					.getHostAddress(), link.localPort);
			while (true) {
				byte[] data = link.readDataBlock();

				for (int i = 0; i < link.length; i++) {
					MAVLinkPacket mavPacket = link.parser.mavlink_parse_char(data[i] & 0x00ff);
					if (mavPacket != null) {
						MAVLinkMessage msg = mavPacket.unpack();
						// System.out.println("decoded:" + msg.toString());
						mavlinkHandler.receiveData(msg);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Closing socket");
		link.socket.close();
	}
}