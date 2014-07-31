package org.droidplanner.desktop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.droidplanner.core.MAVLink.MAVLinkStreams.MAVLinkOutputStream;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Type.FirmwareType;

import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

public class Console {

	private static final int PORT = 14550;
	public static Drone drone;
	protected static Parser parser = new Parser();
	private static MavLinkMsgHandler mavlinkHandler;

	public static void main(String[] args) {
		drone = droneFactory();
		mavlinkHandler = new MavLinkMsgHandler(drone);
		OnDroneListener eventListner = new OnDroneListener() {
			@Override
			public void onDroneEvent(DroneEventsType event, Drone drone) {
				System.out.println("Drone Event:" + event.toString());
			}
		};
		drone.events.addDroneListener(eventListner);

		try {
			DatagramSocket socket = new DatagramSocket(PORT);
			socket.setBroadcast(true);
			socket.setReuseAddress(true);
			byte[] receiveData = new byte[1024];

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost()
					.getHostAddress(), PORT);
			while (System.in.available() == 0) {
				socket.receive(receivePacket);
				byte[] data = receivePacket.getData();
				int length = receivePacket.getLength();

				for (int i = 0; i < length; i++) {
					MAVLinkPacket mavPacket = parser.mavlink_parse_char(data[i] & 0x00ff);
					if (mavPacket != null) {
						MAVLinkMessage msg = mavPacket.unpack();
						System.out.println("decoded:" + msg.toString());
						mavlinkHandler.receiveData(msg);
					}
				}
			}
			System.out.println("Closing socket");
			socket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Drone droneFactory() {
		MAVLinkOutputStream MAVClient = new MAVLinkOutputStream() {

			@Override
			public void toggleConnectionState() {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendMavPacket(MAVLinkPacket pack) {
				// TODO Auto-generated method stub

			}

			@Override
			public void queryConnectionState() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isConnected() {
				// TODO Auto-generated method stub
				return false;
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

}
