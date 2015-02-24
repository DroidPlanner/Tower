package org.droidplanner.android.utils.file.IO;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.droidplanner.android.utils.file.FileStream;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.Messages.MAVLinkMessage;

/**
 * Read tlog file w/ optional message filter
 *
 * <timestamp><MavLink packet>...
 *
 * See http://qgroundcontrol.org/mavlink for details
 *
 */
public class TLogReader {

    private static final String TAG = TLogReader.class.getSimpleName();
    public static final int MSGFILTER_NONE = -1;

    public static class Event
    {
        private long timestamp;
        private MAVLinkMessage mavLinkMessage;

        public Event(long timestamp, MAVLinkMessage mavLinkMessage) {
            this.timestamp = timestamp;
            this.mavLinkMessage = mavLinkMessage;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public MAVLinkMessage getMavLinkMessage() {
            return mavLinkMessage;
        }
    }

    private static final int TIMESTAMP_SIZE = Long.SIZE / Byte.SIZE;

    private final int msgFilter;
    private final List<Event> logEvents = new LinkedList<Event>();


    public TLogReader(int msgFilter) {
        this.msgFilter = msgFilter;
    }

    public List<Event> getLogEvents() {
        return logEvents;
    }

    public boolean openTLog(FileDescriptor fd){
        final Parser parser = new Parser();
        DataInputStream in = null;
        try {
            // open file
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(fd)));

            // read events, filter (if specified)
            long timestamp;
            long prevTimestamp = 0;
            while(in.available() > 0) {
                // read timestamp
                timestamp = in.readLong() / 1000;

                // read packet
                MAVLinkPacket packet;
                while((packet = parser.mavlink_parse_char(in.readUnsignedByte())) == null);

                if(msgFilter == MSGFILTER_NONE || packet.msgid == msgFilter) {
                    if((timestamp - prevTimestamp) > 5000) {
                        logEvents.add(new Event(timestamp, packet.unpack()));
                        prevTimestamp = timestamp;
                    }
                }
            }

        } catch (EOFException e) {
            // NOP - file may be incomplete - take what we have
            // fall thru...
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            // close file
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // NOP
                }
            }
        }

        return true;
    }

    public boolean openTLog(String file) {
        if (!FileStream.isExternalStorageAvailable()) {
            return false;
        }

        final Parser parser = new Parser();
        DataInputStream in = null;
        try {
            // open file
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

            // read events, filter (if specified)
            long timestamp;
            long prevTimestamp = 0;
            while(in.available() > 0) {
                // read timestamp
                timestamp = in.readLong() / 1000;

                // read packet
                MAVLinkPacket packet;
                while((packet = parser.mavlink_parse_char(in.readUnsignedByte())) == null);

                if(msgFilter == MSGFILTER_NONE || packet.msgid == msgFilter) {
                    if((timestamp - prevTimestamp) > 5000) {
                        logEvents.add(new Event(timestamp, packet.unpack()));
                        prevTimestamp = timestamp;
            }
                }
            }

        } catch (EOFException e) {
            // NOP - file may be incomplete - take what we have
            // fall thru...
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            // close file
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // NOP
                }
            }
        }

        return true;
    }
}