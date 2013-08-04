package com.MAVLink.Messages;

import android.util.Log;

/**
 * Storage for MAVLink Packet and Error statistics
 * 
 * @author Helibot
 * 
 */
public class MAVLinkStats /* implements Serializable */{

	public int receivedPacketCount;

	public int crcErrorCount;

	public int lostPacketCount;

	private int lastPacketSeq;

	/**
	 * Check the new received packet to see if has lost someone between this and
	 * the last packet
	 * 
	 * @param packet
	 *            Packet that should be checked
	 */
	public void newPacket(MAVLinkPacket packet) {
		Log.d("MAVLINK", "rxPkt " + packet.seq + "," + lastPacketSeq + ","
				+ lostPacketCount + "," + crcErrorCount + ","
				+ receivedPacketCount);
		// find the expected seq number (and wrap from 255 to 0 if necessary)
		lastPacketSeq = (lastPacketSeq + 1) & 0xFF;

		// We have lost at least one packet
		if (lastPacketSeq > 0 && packet.seq != lastPacketSeq) {
			if (packet.seq - lastPacketSeq < 0)
				lostPacketCount = lostPacketCount
						+ (255 + (packet.seq - lastPacketSeq));
			else
				lostPacketCount = lostPacketCount
						+ (packet.seq - lastPacketSeq);
		}
		lastPacketSeq = packet.seq;
		receivedPacketCount++;
	}

	/**
	 * Called when a CRC error happens on the parser
	 */
	public void crcError() {
		crcErrorCount++;
	}

	/**
	 * Resets statistics for this MAVLink.
	 */
	public void mavlinkResetStats() {
		lastPacketSeq = -1;
		lostPacketCount = 0;
		crcErrorCount = 0;
		receivedPacketCount = 0;
	}
	
}