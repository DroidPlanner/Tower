package org.droidplanner.file.IO;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.droidplanner.file.FileManager;
import org.droidplanner.file.FileStream;


/**
 * Write msg_mission_item list as...
 *
 * QGC WPL <VERSION>
 * <INDEX> <CURRENT WP> <COORD FRAME> <COMMAND> <PARAM1> <PARAM2> <PARAM3> <PARAM4> <PARAM5/X/LONGITUDE> <PARAM6/Y/LATITUDE> <PARAM7/Z/ALTITUDE> <AUTOCONTINUE>
 *
 * See http://qgroundcontrol.org/mavlink/waypoint_protocol for details
 *
 */
public class MissionWriter {
    public static boolean write(List<msg_mission_item> msgMissionItems) {
        return write(msgMissionItems, "waypoints");
    }

    public static boolean write(List<msg_mission_item> msgMissionItems, String name) {
		try {
			if (!FileManager.isExternalStorageAvaliable()) {
				return false;
			}
			FileOutputStream out = FileStream.getWaypointFileStream(name);

			writeHeader(out);
			writeMissionItems(out, msgMissionItems);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void writeHeader(FileOutputStream out) throws IOException {
		out.write(String.format(Locale.ENGLISH, "QGC WPL 110\n").getBytes());
	}

	private static void writeMissionItems(FileOutputStream out, List<msg_mission_item> msgMissionItems) throws IOException {
        // for all msgs...
        for (int i = 0, msgMissionItemsSize = msgMissionItems.size(); i < msgMissionItemsSize; i++) {
            final msg_mission_item msg = msgMissionItems.get(i);

            // write msg (TAB delimited)
            out.write(String.format(Locale.ENGLISH,
                    "%d\t0\t%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%d\n",
                    i + 1,
                    msg.frame,
                    msg.command,
                    msg.param1, msg.param2, msg.param3, msg.param4,
                    msg.x, msg.y, msg.z,
                    msg.autocontinue).getBytes());
        }
	}
}
