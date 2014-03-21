package org.droidplanner.MAVLink;

import com.MAVLink.Messages.MAVLinkPacket;

public interface MAVLinkClient {

	void sendMavPacket(MAVLinkPacket pack);

	boolean isConnected();

	void toggleConnectionState();

	void queryConnectionState();


}
