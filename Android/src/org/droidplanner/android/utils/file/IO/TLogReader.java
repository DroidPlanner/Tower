package org.droidplanner.android.utils.file.IO;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Parser;

import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Read tlog file w/ optional message filter
 *
 * <timestamp><MavLink packet>...
 *
 * See http://qgroundcontrol.org/mavlink for details
 *
 */
public class TLogReader implements OpenFileDialog.FileReader {

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
    private final List<Event> logEvents = new ArrayList<Event>();


    public TLogReader(int msgFilter) {
        this.msgFilter = msgFilter;
    }

    public List<Event> getLogEvents() {
        return logEvents;
    }

    private boolean openTLog(String file) {
		if (!FileManager.isExternalStorageAvaliable()) {
			return false;
		}

        final Parser parser = new Parser();
        DataInputStream in = null;
        try {
            // open file
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

			// read events, filter (if specified)
            long timestamp;
            while(in.available() > 0) {
                // read timestamp
                timestamp = in.readLong() / 1000;

                // read packet
                MAVLinkPacket packet;
                while((packet = parser.mavlink_parse_char(in.readUnsignedByte())) == null);

                if(msgFilter == MSGFILTER_NONE || packet.msgid == msgFilter)
                    logEvents.add(new Event(timestamp, packet.unpack()));
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

	@Override
	public String getPath() {
		return DirectoryPath.getLogPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getTLogFileList();
	}

	@Override
	public boolean openFile(String file) {
		return openTLog(file);
	}
}
