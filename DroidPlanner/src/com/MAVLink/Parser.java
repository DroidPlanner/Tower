package com.MAVLink;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

public class Parser {

	/**
	 * States from the parsing state machine
	 */
	enum MAV_states {
		MAVLINK_PARSE_STATE_UNINIT, MAVLINK_PARSE_STATE_IDLE, MAVLINK_PARSE_STATE_GOT_STX, MAVLINK_PARSE_STATE_GOT_LENGTH, MAVLINK_PARSE_STATE_GOT_SEQ, MAVLINK_PARSE_STATE_GOT_SYSID, MAVLINK_PARSE_STATE_GOT_COMPID, MAVLINK_PARSE_STATE_GOT_MSGID, MAVLINK_PARSE_STATE_GOT_CRC1, MAVLINK_PARSE_STATE_GOT_PAYLOAD
	}

	MAV_states state = MAV_states.MAVLINK_PARSE_STATE_UNINIT;

	static boolean msg_received;

	private MAVLinkPacket m;

	MAVLinkMessage message;

	/**
	 * This is a convenience function which handles the complete MAVLink
	 * parsing. the function will parse one byte at a time and return the
	 * complete packet once it could be successfully decoded. Checksum and other
	 * failures will be silently ignored.
	 * 
	 * @param c
	 *            The char to parse
	 */
	public MAVLinkMessage mavlink_parse_char(int c) {
		msg_received = false;
		message = null;

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
				Log.e("CRC", "Invalid CRC, msgid:"+m.msgid);
				msg_received = false;
				state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
				if (c == MAVLinkPacket.MAVLINK_STX) {
					state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
					m.crc.start_checksum();
				}
			} else {
				state = MAV_states.MAVLINK_PARSE_STATE_GOT_CRC1;
			}
			break;

		case MAVLINK_PARSE_STATE_GOT_CRC1:
			// Check second checksum byte
			if (c != m.crc.getMSB()) {
				Log.e("CRC", "Invalid CRC");
				msg_received = false;
				state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
				if (c == MAVLinkPacket.MAVLINK_STX) {
					state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
					m.crc.start_checksum();
				}
			} else { // Successfully received the message
				try {
					message = m.unpack();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				msg_received = true;
				state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
			}

			break;

		}

		return message;
	}

}
