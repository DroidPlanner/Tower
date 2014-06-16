package org.droidplanner.core.bus.events;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

import org.droidplanner.core.drone.variables.HeartBeat;

/**
 * This class encapsulate a drone hearbeat event.
 */
public class DroneHeartBeatEvent implements DroneEvent {

    private final msg_heartbeat mHeartBeat;
    private final HeartBeat.HeartbeatState mState;

    public DroneHeartBeatEvent(msg_heartbeat msg, HeartBeat.HeartbeatState state){
        mHeartBeat = msg;
        mState = state;
    }

    public msg_heartbeat getHeartBeat(){
        return mHeartBeat;
    }

    public HeartBeat.HeartbeatState getState(){
        return mState;
    }
}
