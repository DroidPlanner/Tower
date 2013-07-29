package com.droidplanner.dialogs;

import com.droidplanner.drone.Drone;
import com.droidplanner.file.IO.MissionReader;

public abstract class OpenMissionDialog extends OpenFileDialog {
	public abstract void waypointFileLoaded(MissionReader reader);

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
		waypointFileLoaded((MissionReader) reader);
	}
}
