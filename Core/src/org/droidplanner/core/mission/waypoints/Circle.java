package org.droidplanner.core.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import android.util.Log;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;

public class Circle extends SpatialCoordItem {

	private double radius = 7.0;
	private int turns = 1;
	private int numberOfSteps = 1;
	private double altitudeStep = 2;

	public Circle(MissionItem item) {
		super(item);
	}

	public Circle(Mission mission, Coord3D coord) {
		super(mission, coord);
	}

	public Circle(msg_mission_item msg, Mission mission) {
		super(mission, null);
		unpackMAVMessage(msg);
	}

	public void setTurns(int turns) {
		this.turns = (int)Math.abs(turns);
	}
	
	public int getNumeberOfTurns() {
		return turns;
	}

	public double getRadius() {
		return radius;
	}
	
	public double getAltitudeStep(){
		return altitudeStep;
	}
	
	public int getNumberOfSteps(){
		return numberOfSteps;
	}
	
	public void setMultiCircle(int number, double stepHeight){
		this.numberOfSteps = number;
		this.altitudeStep = stepHeight;
	}
	
	public void setSingleCircle(){
		numberOfSteps = 1;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		
		for (int i = 0; i < getNumberOfSteps(); i++) {
			Length extraHeight = new Length(getAltitudeStep()*i);
			packSingleCircle(list,extraHeight);			
		}
		
		Log.d("CIRCLE", list.toString());
		return list;
	}

	private void packSingleCircle(List<msg_mission_item> list, Length extraHeight) {
		msg_mission_item mavMsg = new msg_mission_item();
		list.add(mavMsg);
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		mavMsg.x = (float) coordinate.getLat();
		mavMsg.y = (float) coordinate.getLng();
		mavMsg.z = (float) (coordinate.getAltitude().valueInMeters() + extraHeight.valueInMeters());
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TURNS;
		mavMsg.param1 = Math.abs(turns);
		mavMsg.param3 = (turns > 0) ? 1 : -1;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setTurns((int) mavMsg.param1);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.CIRCLE;
	}

}