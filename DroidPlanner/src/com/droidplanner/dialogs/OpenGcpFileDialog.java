package com.droidplanner.dialogs;

import java.util.List;

import com.droidplanner.file.IO.GcpReader;
import com.droidplanner.gcp.Gcp;

public abstract class OpenGcpFileDialog extends OpenFileDialog {
	public abstract void onGcpFileLoaded(List<Gcp> gcpList);

	@Override
	protected FileReader createReader() {
		return new GcpReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		onGcpFileLoaded(((GcpReader) reader).gcpList);
	}
}
