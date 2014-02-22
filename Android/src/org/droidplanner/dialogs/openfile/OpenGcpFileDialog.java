package org.droidplanner.dialogs.openfile;

import java.util.List;

import org.droidplanner.file.IO.GcpReader;
import org.droidplanner.gcp.Gcp;


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
