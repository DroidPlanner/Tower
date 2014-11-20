package org.droidplanner.core.MAVLink;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;

public class MAVLinkStreams {

	public interface MAVLinkOutputStream {

		void sendMavPacket(MAVLinkPacket pack);

		boolean isConnected();

		void toggleConnectionState();

		void queryConnectionState();

	}

	public interface MavlinkInputStream {
		public void notifyConnected();

		public void notifyDisconnected();

		public void notifyReceivedData(MAVLinkMessage m);
	}
}
