package org.droidplanner.android.dialogs.openfile;

import org.droidplanner.android.utils.file.IO.MissionReader;
import org.droidplanner.core.model.Drone;

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
