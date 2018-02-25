package org.droidplanner.services.android.impl.utils;

import android.content.Context;
import android.net.Uri;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.util.ParcelableUtils;
import com.o3dr.services.android.lib.util.UriUtils;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.commands.CameraTriggerImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ChangeSpeedImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ConditionYawImpl;
import org.droidplanner.services.android.impl.core.mission.commands.DoJumpImpl;
import org.droidplanner.services.android.impl.core.mission.commands.EpmGripperImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ReturnToHomeImpl;
import org.droidplanner.services.android.impl.core.mission.commands.SetRelayImpl;
import org.droidplanner.services.android.impl.core.mission.commands.SetServoImpl;
import org.droidplanner.services.android.impl.core.mission.commands.TakeoffImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.CircleImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.DoLandStartImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.LandImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.RegionOfInterestImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.SplineWaypointImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.WaypointImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by fhuya on 8/13/2016.
 */
public class MissionUtils {

    /**
     * Waypoint file format:
     *
     * * QGC WPL <VERSION> <INDEX> <CURRENT WP> <COORD FRAME> <COMMAND> <PARAM1>
     * <PARAM2> <PARAM3> <PARAM4> <PARAM5/X/LONGITUDE> <PARAM6/Y/LATITUDE>
     * <PARAM7/Z/ALTITUDE> <AUTOCONTINUE>
     *
     * See http://qgroundcontrol.org/mavlink/waypoint_protocol for details
     */
    private static final String WAYPOINT_PROTOCOL_HEADER = "QGC WPL 110";

    private MissionUtils(){}

    public static void saveMission(Context context, Mission mission, Uri saveUri, ICommandListener listener){
        saveMissionToWPL(context, mission, saveUri, listener);
    }

    public static Mission loadMission(Context context, Uri loadUri){
        // Attempt to load the mission using the waypoint file format.
        Mission mission = loadMissionFromWPL(context, loadUri);
        if(mission == null){
            // Attempt to load using the deprecated dpwp formt
            mission = loadMissionFromDpwp(context, loadUri);
        }
        return mission;
    }

    private static void saveMissionToWPL(Context context, Mission mission, Uri saveUri, ICommandListener listener){
        try {
            OutputStream saveOut = UriUtils.getOutputStream(context, saveUri);
            try {
                String header = WAYPOINT_PROTOCOL_HEADER + "\n";
                saveOut.write(header.getBytes());

                // Get the list of msg mission item
                List<msg_mission_item> rawMissionItems = fromMission(mission);
                for (int i = 0, msgMissionItemsSize = rawMissionItems.size(); i < msgMissionItemsSize; i++) {
                    final msg_mission_item msg = rawMissionItems.get(i);

                    // write msg (TAB delimited)
                    saveOut.write(String.format(Locale.ENGLISH,
                            "%d\t%d\t%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%d\n",
                            i,
                            i == 0 ? 1 : 0, // set CURRENT_WP = 1 for 'home' - msg[0], 0
                            // for all others
                            msg.frame, msg.command, msg.param1, msg.param2, msg.param3, msg.param4, msg.x,
                            msg.y, msg.z, msg.autocontinue).getBytes());
                }

                CommonApiUtils.postSuccessEvent(listener);
            }finally{
                saveOut.close();
            }
        } catch (IOException e) {
            Timber.e(e, "Unable to write to uri %s", saveUri);
            CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
        }
    }

    private static void saveMissionToDpwp(Context context, Mission mission, Uri saveUri, ICommandListener listener){
        try{
            OutputStream saveOut = UriUtils.getOutputStream(context, saveUri);
            try{
                byte[] missionBytes = ParcelableUtils.marshall(mission);
                saveOut.write(missionBytes);
                CommonApiUtils.postSuccessEvent(listener);
            }finally{
                saveOut.close();
            }

        } catch(IOException e){
            Timber.e(e, "Unable to write to uri %s", saveUri);
            CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
        }
    }

    private static Mission loadMissionFromDpwp(Context context, Uri loadUri){
        try{
            InputStream loadIn = UriUtils.getInputStream(context, loadUri);
            try{
                Map<byte[], Integer> bytesList = new LinkedHashMap<>();
                int length = 0;
                while(loadIn.available() > 0){
                    byte[] missionBytes = new byte[2048];
                    int bufferSize = loadIn.read(missionBytes);
                    bytesList.put(missionBytes, bufferSize);
                    length += bufferSize;
                }

                ByteBuffer fullBuffer = ByteBuffer.allocate(length);
                for(Map.Entry<byte[], Integer> entry : bytesList.entrySet()){
                    fullBuffer.put(entry.getKey(), 0, entry.getValue());
                }

                Mission mission = ParcelableUtils.unmarshall(fullBuffer.array(), 0, length, Mission.CREATOR);
                return mission;
            }finally{
                loadIn.close();
            }
        }catch(IOException e){
            Timber.e(e, "Unable to read from uri %s", loadUri);
            return null;
        }
    }
    
    private static Mission loadMissionFromWPL(Context context, Uri loadUri) {
        try {
            InputStream loadIn = UriUtils.getInputStream(context, loadUri);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(loadIn));

                if (!reader.readLine().contains(WAYPOINT_PROTOCOL_HEADER)) {
                    // Invalid file header.
                    Timber.w("Invalid waypoint file format for %s", loadUri);
                    return null;
                }

                List<msg_mission_item> rawMissionItems = new LinkedList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    // parse line (TAB delimited)
                    final String[] rowData = line.split("\t");

                    final msg_mission_item msg = new msg_mission_item();
                    msg.seq = (Short.parseShort(rowData[0]));
                    msg.current = (Byte.parseByte(rowData[1]));
                    msg.frame = (Byte.parseByte(rowData[2]));
                    msg.command = (Short.parseShort(rowData[3]));

                    msg.param1 = (Float.parseFloat(rowData[4]));
                    msg.param2 = (Float.parseFloat(rowData[5]));
                    msg.param3 = (Float.parseFloat(rowData[6]));
                    msg.param4 = (Float.parseFloat(rowData[7]));

                    msg.x = (Float.parseFloat(rowData[8]));
                    msg.y = (Float.parseFloat(rowData[9]));
                    msg.z = (Float.parseFloat(rowData[10]));

                    msg.autocontinue = (Byte.parseByte(rowData[11]));

                    rawMissionItems.add(msg);
                }

                return fromRawMissionItems(rawMissionItems);
            }finally{
                loadIn.close();
            }
        } catch (IOException e) {
            Timber.e(e, "Unable to load mission from uri %s", loadUri);
            return null;
        }
    }
    
    private static List<msg_mission_item> fromMission(Mission mission){
        if(mission == null)
            return null;
        
        List<MissionItem> missionItems = mission.getMissionItems();
        if(missionItems.isEmpty())
            return Collections.emptyList();
        
        MissionImpl missionImpl = new MissionImpl(null);
        List<msg_mission_item> rawMissionItems = new ArrayList<>(missionItems.size());
        for(MissionItem missionItem : missionItems){
            MissionItemImpl impl = ProxyUtils.getMissionItemImpl(missionImpl, missionItem);
            rawMissionItems.addAll(impl.packMissionItem());
        }
        
        return rawMissionItems;
    }
    
    private static Mission fromRawMissionItems(List<msg_mission_item> rawMissionItems){
        Mission mission = new Mission();
        if(rawMissionItems == null || rawMissionItems.isEmpty())
            return mission;

        List<MissionItemImpl> impls = processMavLinkMessages(new MissionImpl(null), rawMissionItems);
        if(!impls.isEmpty()) {
            for (MissionItemImpl impl: impls) {
                MissionItem missionItem = ProxyUtils.getProxyMissionItem(impl);
                if(missionItem != null){
                    mission.addMissionItem(missionItem);
                }
            }
        }
        return mission;
    }

    public static List<MissionItemImpl> processMavLinkMessages(MissionImpl missionImpl, List<msg_mission_item> msgs) {
        List<MissionItemImpl> received = new ArrayList<MissionItemImpl>();
        for (msg_mission_item msg : msgs) {
            switch (msg.command) {
                case MAV_CMD.MAV_CMD_DO_SET_SERVO:
                    received.add(new SetServoImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
                    received.add(new WaypointImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_NAV_SPLINE_WAYPOINT:
                    received.add(new SplineWaypointImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_NAV_LAND:
                    received.add(new LandImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_DO_LAND_START:
                    received.add(new DoLandStartImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
                    received.add(new TakeoffImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_DO_CHANGE_SPEED:
                    received.add(new ChangeSpeedImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST:
                    received.add(new CameraTriggerImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_DO_GRIPPER:
                    received.add(new EpmGripperImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_DO_SET_ROI:
                    received.add(new RegionOfInterestImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS:
                    received.add(new CircleImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH:
                    received.add(new ReturnToHomeImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_CONDITION_YAW:
                    received.add(new ConditionYawImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_DO_SET_RELAY:
                    received.add(new SetRelayImpl(msg, missionImpl));
                    break;
                case MAV_CMD.MAV_CMD_DO_JUMP:
                    received.add(new DoJumpImpl(msg, missionImpl));
                    break;

                default:
                    break;
            }
        }
        return received;
    }
}
