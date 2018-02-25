package org.droidplanner.services.android.impl.core.mission;

import android.util.Pair;

import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameter;

import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneVariable;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.APMConstants;
import org.droidplanner.services.android.impl.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.impl.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.impl.core.mission.commands.ChangeSpeedImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ReturnToHomeImpl;
import org.droidplanner.services.android.impl.core.mission.commands.TakeoffImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.LandImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.RegionOfInterestImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.services.android.impl.core.mission.waypoints.WaypointImpl;
import org.droidplanner.services.android.impl.utils.MissionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This implements a mavlink mission. A mavlink mission is a set of
 * commands/mission items to be carried out by the drone.
 */
public class MissionImpl extends DroneVariable<GenericMavLinkDrone> {

    /**
     * Stores the set of mission items belonging to this mission.
     */
    private List<MissionItemImpl> items = new ArrayList<MissionItemImpl>();
    private final List<MissionItemImpl> componentItems = new ArrayList<>();

    public MissionImpl(GenericMavLinkDrone myDrone) {
        super(myDrone);
    }

    /**
     * Removes a waypoint from the mission's set of mission items.
     *
     * @param item waypoint to remove
     */
    public void removeWaypoint(MissionItemImpl item) {
        items.remove(item);
        notifyMissionUpdate();
    }

    /**
     * Removes a list of waypoints from the mission's set of mission items.
     *
     * @param toRemove list of waypoints to remove
     */
    public void removeWaypoints(List<MissionItemImpl> toRemove) {
        items.removeAll(toRemove);
        notifyMissionUpdate();
    }

    /**
     * Add a list of waypoints to the mission's set of mission items.
     *
     * @param missionItemImpls list of waypoints to add
     */
    public void addMissionItems(List<MissionItemImpl> missionItemImpls) {
        items.addAll(missionItemImpls);
        notifyMissionUpdate();
    }

    public void clearMissionItems() {
        items.clear();
        notifyMissionUpdate();
    }

    /**
     * Add a waypoint to the mission's set of mission item.
     *
     * @param missionItemImpl waypoint to add
     */
    public void addMissionItem(MissionItemImpl missionItemImpl) {
        items.add(missionItemImpl);
        notifyMissionUpdate();
    }

    public void addMissionItem(int index, MissionItemImpl missionItemImpl) {
        items.add(index, missionItemImpl);
        notifyMissionUpdate();
    }

    /**
     * Signals that this mission object was updated.
     */
    public void notifyMissionUpdate() {
        updateComponentItems();
        myDrone.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
    }

    /**
     * Updates a mission item
     *
     * @param oldItem mission item to update
     * @param newItem new mission item
     */
    public void replace(MissionItemImpl oldItem, MissionItemImpl newItem) {
        final int index = items.indexOf(oldItem);
        if (index == -1) {
            return;
        }

        items.remove(index);
        items.add(index, newItem);
        notifyMissionUpdate();
    }

    public void replaceAll(List<Pair<MissionItemImpl, MissionItemImpl>> updatesList) {
        if (updatesList == null || updatesList.isEmpty()) {
            return;
        }

        boolean wasUpdated = false;
        for (Pair<MissionItemImpl, MissionItemImpl> updatePair : updatesList) {
            final MissionItemImpl oldItem = updatePair.first;
            final int index = items.indexOf(oldItem);
            if (index == -1) {
                continue;
            }

            final MissionItemImpl newItem = updatePair.second;
            items.remove(index);
            items.add(index, newItem);

            wasUpdated = true;
        }

        if (wasUpdated) {
            notifyMissionUpdate();
        }
    }

    /**
     * Reverse the order of the mission items.
     */
    public void reverse() {
        Collections.reverse(items);
        notifyMissionUpdate();
    }

    public void onWriteWaypoints(msg_mission_ack msg) {
        myDrone.notifyDroneEvent(DroneEventsType.MISSION_SENT);
    }

    public List<MissionItemImpl> getItems() {
        return items;
    }
    public List<MissionItemImpl> getComponentItems(){
        return componentItems;
    }

    public int getOrder(MissionItemImpl waypoint) {
        return items.indexOf(waypoint) + 1; // plus one to account for the fact
        // that this is an index
    }

    public double getAltitudeDiffFromPreviousItem(SpatialCoordItem waypoint) throws IllegalArgumentException {
        int i = items.indexOf(waypoint);
        if (i > 0) {
            MissionItemImpl previous = items.get(i - 1);
            if (previous instanceof SpatialCoordItem) {
                return waypoint.getCoordinate().getAltitude() - ((SpatialCoordItem) previous).getCoordinate()
                        .getAltitude();
            }
        }
        throw new IllegalArgumentException("Last waypoint doesn't have an altitude");
    }

    public double getDistanceFromLastWaypoint(SpatialCoordItem waypoint)
            throws IllegalArgumentException {
        int i = items.indexOf(waypoint);
        if (i > 0) {
            MissionItemImpl previous = items.get(i - 1);
            if (previous instanceof SpatialCoordItem) {
                return GeoTools.getDistance(waypoint.getCoordinate(),
                        ((SpatialCoordItem) previous).getCoordinate());
            }
        }
        throw new IllegalArgumentException("Last waypoint doesn't have a coordinate");
    }

    public boolean hasItem(MissionItemImpl item) {
        return items.contains(item);
    }

    public void onMissionReceived(List<msg_mission_item> msgs) {
        if (msgs != null) {
            myDrone.processHomeUpdate(msgs.get(0));
            msgs.remove(0); // Remove Home waypoint
            items.clear();
            items.addAll(MissionUtils.processMavLinkMessages(this, msgs));
            myDrone.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
            notifyMissionUpdate();
        }
    }

    public void onMissionLoaded(List<msg_mission_item> msgs) {
        if (msgs != null) {
            myDrone.processHomeUpdate(msgs.get(0));
            msgs.remove(0); // Remove Home waypoint
            items.clear();
            items.addAll(MissionUtils.processMavLinkMessages(this, msgs));
            myDrone.notifyDroneEvent(DroneEventsType.MISSION_RECEIVED);
            notifyMissionUpdate();
        }
    }

    /**
     * Sends the mission to the drone using the mavlink protocol.
     */
    public void sendMissionToAPM() {
        List<msg_mission_item> msgMissionItems = getMsgMissionItems();
        myDrone.getWaypointManager().writeWaypoints(msgMissionItems);
        updateComponentItems(msgMissionItems);
    }

    private void updateComponentItems(){
        List<msg_mission_item> msgMissionItems = getMsgMissionItems();
        updateComponentItems(msgMissionItems);
    }

    private void updateComponentItems(List<msg_mission_item> msgMissionItems) {
        componentItems.clear();
        if(msgMissionItems == null || msgMissionItems.isEmpty()) {
            return;
        }
        msg_mission_item firstItem = msgMissionItems.get(0);
        if(firstItem.seq == APMConstants.HOME_WAYPOINT_INDEX) {
            msgMissionItems.remove(0); // Remove Home waypoint
        }
        componentItems.addAll(MissionUtils.processMavLinkMessages(this, msgMissionItems));
    }

    public msg_mission_item packHomeMavlink() {
        Home home = (Home) myDrone.getAttribute(AttributeType.HOME);
        LatLongAlt coordinate = home.getCoordinate();

        msg_mission_item mavMsg = new msg_mission_item();
        mavMsg.autocontinue = 1;
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
        mavMsg.current = 0;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
        mavMsg.target_system = myDrone.getSysid();
        mavMsg.target_component = myDrone.getCompid();
        if (home.isValid()) {
            mavMsg.x = (float) coordinate.getLatitude();
            mavMsg.y = (float) coordinate.getLongitude();
            mavMsg.z = (float) coordinate.getAltitude();
        }

        return mavMsg;
    }

    public List<msg_mission_item> getMsgMissionItems() {
        List<msg_mission_item> data = new ArrayList<msg_mission_item>();
        int waypointCount = 0;
        msg_mission_item home = packHomeMavlink();
        home.seq = waypointCount++;
        data.add(home);

        int size = items.size();
        for (int i = 0; i < size; i++) {
            MissionItemImpl item = items.get(i);
            for(msg_mission_item msg_item: item.packMissionItem()){
                msg_item.seq = waypointCount++;
                data.add(msg_item);
            }
        }
        return data;
    }

    /**
     * Create and upload a dronie mission to the drone
     *
     * @return the bearing in degrees the drone trajectory will take.
     */
    public double makeAndUploadDronie() {
        final Gps droneGps = (Gps) myDrone.getAttribute(AttributeType.GPS);
        LatLong currentPosition = droneGps.getPosition();
        if (currentPosition == null || droneGps.getSatellitesCount() <= 5) {
            myDrone.notifyDroneEvent(DroneEventsType.WARNING_NO_GPS);
            return -1;
        }

        final Attitude attitude = (Attitude) myDrone.getAttribute(AttributeType.ATTITUDE);
        final double bearing = 180 + attitude.getYaw();
        items.clear();
        items.addAll(createDronie(currentPosition,
                GeoTools.newCoordFromBearingAndDistance(currentPosition, bearing, 50.0)));
        sendMissionToAPM();
        notifyMissionUpdate();

        return bearing;
    }

    private double getSpeedParameter(){
        Parameter param = myDrone.getParameterManager().getParameter("WPNAV_SPEED");
        if (param == null ) {
            return -1;
        }else{
            return (param.getValue()/100);
        }

    }

    public List<MissionItemImpl> createDronie(LatLong start, LatLong end) {
        final int startAltitude = 4;
        final int roiDistance = -8;
        LatLong slowDownPoint = GeoTools.pointAlongTheLine(start, end, 5);

        double defaultSpeed = getSpeedParameter();
        if (defaultSpeed == -1) {
            defaultSpeed = 5;
        }

        List<MissionItemImpl> dronieItems = new ArrayList<MissionItemImpl>();
        dronieItems.add(new TakeoffImpl(this, startAltitude));
        dronieItems.add(new RegionOfInterestImpl(this,
                new LatLongAlt(GeoTools.pointAlongTheLine(start, end, roiDistance), (1.0))));
        dronieItems.add(new WaypointImpl(this, new LatLongAlt(end, (startAltitude + GeoTools.getDistance(start, end) / 2.0))));
        dronieItems.add(new WaypointImpl(this,
                new LatLongAlt(slowDownPoint, (startAltitude + GeoTools.getDistance(start, slowDownPoint) / 2.0))));
        dronieItems.add(new ChangeSpeedImpl(this, 1.0));
        dronieItems.add(new WaypointImpl(this, new LatLongAlt(start, startAltitude)));
        dronieItems.add(new ChangeSpeedImpl(this, defaultSpeed));
        dronieItems.add(new LandImpl(this, start));
        return dronieItems;
    }

    public boolean hasTakeoffAndLandOrRTL() {
        if (items.size() >= 2) {
            if (isFirstItemTakeoff() && isLastItemLandOrRTL()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFirstItemTakeoff() {
        return !items.isEmpty() && items.get(0) instanceof TakeoffImpl;
    }

    public boolean isLastItemLandOrRTL() {
        if (items.isEmpty())
            return false;

        MissionItemImpl last = items.get(items.size() - 1);
        return (last instanceof ReturnToHomeImpl) || (last instanceof LandImpl);
    }
}
