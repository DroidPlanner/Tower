package com.MAVLink.Messages;

/**
 * Storage for MAVLink Pkt and Error statistics
 * 
 * @author Helibot
 *
 */
public class MAVLinkStats /*implements Serializable*/ {

	/** Received pkt count - Count of successfully received packets */
	public int pkt_cnt;
	
	public int crc_error_cnt;

	public int last_seq;
	
	public int lost_seq_cnt;

}