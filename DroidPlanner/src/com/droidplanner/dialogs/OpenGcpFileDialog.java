package com.droidplanner.dialogs;

import java.util.List;

import com.droidplanner.helpers.KmlParser;
import com.droidplanner.waypoints.gcp;

public abstract class OpenGcpFileDialog extends OpenFileDialog {
	public abstract void onGcpFileLoaded(List<gcp> gcpList);

	@Override
	protected FileReader createReader() {
		return new KmlParser();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		onGcpFileLoaded(((KmlParser) reader).gcpList);
	}
}
