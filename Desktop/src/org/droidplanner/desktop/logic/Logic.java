package org.droidplanner.desktop.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;
import org.droidplanner.desktop.communication.Connection;
import org.droidplanner.desktop.location.FakeLocation;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class Logic implements Runnable, OnDroneListener {
	public Drone drone;
	public Follow follow;
	private MavLinkMsgHandler mavlinkHandler;
	protected Connection link = new Connection(14550);
	
	ArrayList<ThreeSpacePoint> points = new ArrayList<ThreeSpacePoint>();

	public Logic() {
		drone = new DroneImpl(link, FakeFactory.fakeClock(), FakeFactory.fakeHandler(),
				FakeFactory.fakePreferences());
		mavlinkHandler = new MavLinkMsgHandler(drone);
		follow = new Follow(drone, FakeFactory.fakeHandler(), new FakeLocation());
		drone.addDroneListener(this);
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

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MAGNETOMETER:
			int[] magVector = drone.getMagnetometer().getVector();
			ThreeSpacePoint point = new ThreeSpacePoint(magVector[0],magVector[1],magVector[2]);
			points.add(point);

			FitPoints ellipsoidFit = new FitPoints();
			ellipsoidFit.fitEllipsoid(points);
			

			System.out.println("IMU"+ Arrays.toString(magVector)+" \t"+points.size()+"\tCenter:" + ellipsoidFit.center.toString()+ "\tRadii:" + ellipsoidFit.radii.toString()+"\t\t Eigenvector"+ellipsoidFit.evecs);
			MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0, 50, 0);
			break;

		case HEARTBEAT_FIRST:
			MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0, 50, 0);
			break;
		default:
			break;
		}

	}
	
}