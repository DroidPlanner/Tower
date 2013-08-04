package com.MAVLink;

import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkStats;
import android.util.Log;

public class Parser {

	/**
	 * States from the parsing state machine
	 */
	enum MAV_states {
		MAVLINK_PARSE_STATE_UNINIT, MAVLINK_PARSE_STATE_IDLE, MAVLINK_PARSE_STATE_GOT_STX, MAVLINK_PARSE_STATE_GOT_LENGTH, MAVLINK_PARSE_STATE_GOT_SEQ, MAVLINK_PARSE_STATE_GOT_SYSID, MAVLINK_PARSE_STATE_GOT_COMPID, MAVLINK_PARSE_STATE_GOT_MSGID, MAVLINK_PARSE_STATE_GOT_CRC1, MAVLINK_PARSE_STATE_GOT_PAYLOAD
	}

	MAV_states state = MAV_states.MAVLINK_PARSE_STATE_UNINIT;

	static boolean msg_received;

	private MAVLinkStats s = new MAVLinkStats();
	private MAVLinkPacket m;

	/**
	 * This is a convenience function which handles the complete MAVLink
	 * parsing. the function will parse one byte at a time and return the
	 * complete packet once it could be successfully decoded. Checksum and other
	 * failures will be silently ignored.
	 * 
	 * @param c
	 *            The char to parse
	 */
	public MAVLinkPacket mavlink_parse_char(int c) {
		msg_received = false;
		
		switch (state) {
		case MAVLINK_PARSE_STATE_UNINIT:
		case MAVLINK_PARSE_STATE_IDLE:

			if (c == MAVLinkPacket.MAVLINK_STX) {
				state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
				m = new MAVLinkPacket();
			}
			break;

		case MAVLINK_PARSE_STATE_GOT_STX:
			if (msg_received) {
				msg_received = false;
				state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
			} else {
				m.len = c;
				state = MAV_states.MAVLINK_PARSE_STATE_GOT_LENGTH;
			}
			break;

		case MAVLINK_PARSE_STATE_GOT_LENGTH:
			m.seq = c;
			state = MAV_states.MAVLINK_PARSE_STATE_GOT_SEQ;
			break;

		case MAVLINK_PARSE_STATE_GOT_SEQ:
			m.sysid = c;
			state = MAV_states.MAVLINK_PARSE_STATE_GOT_SYSID;
			break;

		case MAVLINK_PARSE_STATE_GOT_SYSID:
			m.compid = c;
			state = MAV_states.MAVLINK_PARSE_STATE_GOT_COMPID;
			break;

		case MAVLINK_PARSE_STATE_GOT_COMPID:
			m.msgid = c;
			if (m.len == 0) {
				state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
			} else {
				state = MAV_states.MAVLINK_PARSE_STATE_GOT_MSGID;
			}
			break;

		case MAVLINK_PARSE_STATE_GOT_MSGID:
			m.payload.add((byte) c);
			if (m.payloadIsFilled()) {
				state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
			}
			break;

		case MAVLINK_PARSE_STATE_GOT_PAYLOAD:
			m.generateCRC();
			// Check first checksum byte
			if (c != m.crc.getLSB()) {
				msg_received = false;
				state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
				if (c == MAVLinkPacket.MAVLINK_STX) {
					state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
					m.crc.start_checksum();
				}
				s.crc_error_cnt++;
			} else {
				state = MAV_states.MAVLINK_PARSE_STATE_GOT_CRC1;
			}
			break;

		case MAVLINK_PARSE_STATE_GOT_CRC1:
			// Check second checksum byte
			if (c != m.crc.getMSB()) {
				msg_received = false;
				state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
				if (c == MAVLinkPacket.MAVLINK_STX) {
					state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
					m.crc.start_checksum();
				}
				s.crc_error_cnt++;
			} else { // Successfully received the message
				Log.d("MAVLINK", "rxPkt "+ m.seq +"," + s.last_seq + "," + s.lost_seq_cnt +"," + s.crc_error_cnt + "," + s.pkt_cnt);
				s.last_seq = (s.last_seq + 1) & 0xFF; //find the expected seq number (and wrap from 255 to 0 if necessary)
				if (s.last_seq > 0 && m.seq != s.last_seq )
				{  //We have lost at least one packet
					if (m.seq - s.last_seq  < 0)
						s.lost_seq_cnt = s.lost_seq_cnt + (255 +(m.seq-s.last_seq));
					else
						s.lost_seq_cnt = s.lost_seq_cnt + (m.seq-s.last_seq);
				}
				s.last_seq  = m.seq;	
				s.pkt_cnt ++;
				msg_received = true;
				state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
			}

			break;

		}
		if (msg_received) {
			return m;		
		}else {
			return null;
		}
	}

	/**
	 * This function resets error counting for the MAVLink.
	 */
	public void mavlink_reset_counts() {
		s.last_seq = -1;
		s.lost_seq_cnt = 0;
		s.crc_error_cnt = 0;
		s.pkt_cnt = 0;
	}

	
	/**
	 * This function returns counts for the MAVLink.
	 */
public MAVLinkStats get_mavlink_counts() {
		return s;
	}

	
	
}
