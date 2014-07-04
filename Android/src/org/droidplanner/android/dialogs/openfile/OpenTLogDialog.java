package org.droidplanner.android.dialogs.openfile;

import org.droidplanner.android.utils.file.IO.TLogReader;

public abstract class OpenTLogDialog extends OpenFileDialog {
	public abstract void tlogFileLoaded(TLogReader reader);

	@Override
	protected FileReader createReader() {
		return new TLogReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
        tlogFileLoaded((TLogReader) reader);
	}
}
