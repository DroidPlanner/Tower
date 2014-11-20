        package com.MAVLink.common;
        
        /**
        * X.25 CRC calculation for MAVlink messages. The checksum must be initialized,
        * updated with witch field of the message, and then finished with the message
        * id.
        *
        */
        public class CRC {
        private final int[] MAVLINK_MESSAGE_CRCS = {50, 124, 137, 0, 237, 217, 104, 119, 0, 0, 0, 89, 0, 0, 0, 0, 0, 0, 0, 0, 214, 159, 220, 168, 24, 23, 170, 144, 67, 115, 39, 246, 185, 104, 237, 244, 222, 212, 9, 254, 230, 28, 28, 132, 221, 232, 11, 153, 41, 39, 0, 0, 0, 0, 15, 3, 0, 0, 0, 0, 0, 153, 183, 51, 82, 118, 148, 21, 0, 243, 124, 0, 0, 38, 20, 158, 152, 143, 0, 0, 0, 106, 49, 22, 29, 12, 241, 233, 0, 231, 183, 63, 54, 0, 0, 0, 0, 0, 0, 0, 175, 102, 158, 208, 56, 93, 211, 108, 32, 185, 84, 0, 0, 124, 119, 4, 76, 128, 56, 116, 134, 237, 203, 250, 87, 203, 220, 25, 226, 0, 29, 223, 85, 6, 229, 203, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 204, 49, 170, 44, 83, 46, 0};
        private static final int CRC_INIT_VALUE = 0xffff;
        private int CRCvalue;
        
        /**
        * Accumulate the X.25 CRC by adding one char at a time.
        *
        * The checksum function adds the hash of one char at a time to the 16 bit
        * checksum (uint16_t).
        *
        * @param data
        *            new char to hash
        * @param crcAccum
        *            the already accumulated checksum
        **/
        public  void update_checksum(int data) {
		int tmp;
		data= data & 0xff;	//cast because we want an unsigned type
		tmp = data ^ (CRCvalue & 0xff);
		tmp ^= (tmp << 4) & 0xff;
		CRCvalue = ((CRCvalue >> 8) & 0xff) ^ (tmp << 8) ^ (tmp << 3)
        ^ ((tmp >> 4) & 0xf);
        }
        
        /**
        * Finish the CRC calculation of a message, by running the CRC with the
        * Magic Byte. This Magic byte has been defined in MAVlink v1.0.
        *
        * @param msgid
        *            The message id number
        */
        public  void finish_checksum(int msgid) {
		update_checksum(MAVLINK_MESSAGE_CRCS[msgid]);
        }
        
        /**
        * Initialize the buffer for the X.25 CRC
        *
        */
        public void start_checksum() {
		CRCvalue = CRC_INIT_VALUE;
        }
        
        public int getMSB() {
		return ((CRCvalue >> 8) & 0xff);
        }
        
        public int getLSB() {
		return (CRCvalue & 0xff);
        }
        
        public CRC() {
		start_checksum();
        }
        
        }
        