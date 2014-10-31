package org.droidplanner.desktop.logic;

import java.io.IOException;
import java.net.InetAddress;

import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.desktop.communication.Connection;
import org.droidplanner.desktop.location.FakeLocation;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

public class Logic implements Runnable {
	public Drone drone;
	public Follow follow;
	private MavLinkMsgHandler mavlinkHandler;
	protected Connection link = new Connection(14550);
	

	public Logic() {
		Handler handler = new org.droidplanner.desktop.logic.Handler();
		drone = new DroneImpl(link, new Clock(), handler,FakeFactory.fakePreferences());
		mavlinkHandler = new MavLinkMsgHandler(drone);
		follow = new Follow(drone, handler, new FakeLocation());
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
						mavlinkHandler.receiveData(msg);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Closing socket");
		link.socket.close();
	}
	
}