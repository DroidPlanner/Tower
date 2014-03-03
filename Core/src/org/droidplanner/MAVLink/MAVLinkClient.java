package org.droidplanner.MAVLink;

import com.MAVLink.Messages.MAVLinkPacket;

public interface MAVLinkClient {

	void sendMavPacket(MAVLinkPacket pack);

	void setTimeOut();

	void setTimeOut(boolean b);
	
	void resetTimeOut();

	void setTimeOutValue(int value);

	void setTimeOutRetry(int maxRetry);

	int getTimeOutRetry();

	boolean isConnected();

	void toggleConnectionState();

	void queryConnectionState();


}
