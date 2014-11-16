package org.droidplanner.android.utils.file.IO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileManager;
import org.droidplanner.android.utils.file.FileStream;

import com.o3dr.services.android.lib.drone.mission.item.raw.MissionItemMessage;

/**
 * Write msg_mission_item list as...
 * 
 * QGC WPL <VERSION> <INDEX> <CURRENT WP> <COORD FRAME> <COMMAND> <PARAM1>
 * <PARAM2> <PARAM3> <PARAM4> <PARAM5/X/LONGITUDE> <PARAM6/Y/LATITUDE>
 * <PARAM7/Z/ALTITUDE> <AUTOCONTINUE>
 * 
 * See http://qgroundcontrol.org/mavlink/waypoint_protocol for details
 * 
 */
public class MissionWriter {
	public static boolean write(List<MissionItemMessage> msgMissionItems) {
		return write(msgMissionItems, FileStream.getWaypointFilename("waypoints"));
	}

	public static boolean write(List<MissionItemMessage> msgMissionItems, String filename) {
		try {
			if (!FileManager.isExternalStorageAvailable())
				return false;

            if(!filename.endsWith(FileList.WAYPOINT_FILENAME_EXT)){
                filename += FileList.WAYPOINT_FILENAME_EXT;
            }

			final FileOutputStream out = FileStream.getWaypointFileStream(filename);
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

	private static void writeMissionItems(FileOutputStream out,
			List<MissionItemMessage> msgMissionItems) throws IOException {
		// for all msgs...
		for (int i = 0, msgMissionItemsSize = msgMissionItems.size(); i < msgMissionItemsSize; i++) {
			final MissionItemMessage msg = msgMissionItems.get(i);

			// write msg (TAB delimited)
			out.write(String.format(Locale.ENGLISH,
					"%d\t%d\t%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%d\n",
					i,
					i == 0 ? 1 : 0, // set CURRENT_WP = 1 for 'home' - msg[0], 0
									// for all others
					msg.getFrame(), msg.getCommand(), msg.getParam1(), msg.getParam2(), msg.getParam3(), msg.getParam4(), msg.getX(),
					msg.getY(), msg.getZ(), msg.getAutocontinue()).getBytes());
		}
	}
}
