package org.droidplanner.android.utils.file.IO.mission;

import com.MAVLink.common.msg_mission_item;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ExperimentalApi;
import com.o3dr.services.android.lib.drone.mission.Mission;

import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Write msg_mission_item list as...
 *
 * QGC WPL <VERSION> <INDEX> <CURRENT WP> <COORD FRAME> <COMMAND> <PARAM1>
 * <PARAM2> <PARAM3> <PARAM4> <PARAM5/X/LONGITUDE> <PARAM6/Y/LATITUDE>
 * <PARAM7/Z/ALTITUDE> <AUTOCONTINUE>
 *
 * See http://qgroundcontrol.org/mavlink/waypoint_protocol for details
 *
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
public class DeprecatedMissionWriter {
    static final String HEADER = "QGC WPL 110";

    public static boolean write(Drone drone, Mission mission) {
        return write(drone, mission, FileStream.getWaypointFilename("waypoints"));
    }

    public static boolean write(Drone drone, Mission mission, String filename) {
        try {
            if (!FileStream.isExternalStorageAvailable())
                return false;

            if(!filename.endsWith(FileList.WAYPOINT_FILENAME_EXT)){
                filename += FileList.WAYPOINT_FILENAME_EXT;
            }

            final String waypointFilename = filename;
            if(mission.getMissionItems().isEmpty()){
                writeMissionItems(waypointFilename, Collections.<msg_mission_item>emptyList());
            }
            else{
                ExperimentalApi.getApi(drone).convertToRawMessageMissionItems(mission, new ExperimentalApi.MissionItemConversionListener() {
                    @Override
                    public void onMissionItemsConverted(List<msg_mission_item> rawMissionItems) {
                        try {
                            writeMissionItems(waypointFilename, rawMissionItems);
                        } catch (IOException e) {
                            Timber.e(e, "Unable to write mission to %s", waypointFilename);
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void writeMissionItems(String filename,
                                          List<msg_mission_item> msgMissionItems) throws IOException {
        final FileOutputStream out = FileStream.getWaypointFileStream(filename);
        String header = HEADER + "\n";
        out.write(header.getBytes());

        // for all msgs...
        for (int i = 0, msgMissionItemsSize = msgMissionItems.size(); i < msgMissionItemsSize; i++) {
            final msg_mission_item msg = msgMissionItems.get(i);

            // write msg (TAB delimited)
            out.write(String.format(Locale.ENGLISH,
                "%d\t%d\t%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%d\n",
                i,
                i == 0 ? 1 : 0, // set CURRENT_WP = 1 for 'home' - msg[0], 0
                // for all others
                msg.frame, msg.command, msg.param1, msg.param2, msg.param3, msg.param4, msg.x,
                msg.y, msg.z, msg.autocontinue).getBytes());
        }

        out.close();
    }
}
