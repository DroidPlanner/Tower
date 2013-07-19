package com.droidplanner.dialogs;

import com.droidplanner.drone.Drone;
import com.droidplanner.waypoints.MissionReader;

public abstract class OpenMissionDialog extends OpenFileDialog {
	public abstract void waypointFileLoaded();
	
	Drone drone;
	
	public OpenMissionDialog(Drone drone) {
		super();				
		this.drone = drone;
	}

	@Override
	protected FileReader createReader() {
		return new MissionReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		drone.mission.setHome(((MissionReader)reader).getHome());
		drone.mission.setWaypoints(((MissionReader)reader).getWaypoints());
		waypointFileLoaded();				
	}
}
