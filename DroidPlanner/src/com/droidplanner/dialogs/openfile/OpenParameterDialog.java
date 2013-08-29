package com.droidplanner.dialogs.openfile;

import java.util.List;

import com.droidplanner.file.IO.ParameterReader;
import com.droidplanner.parameters.Parameter;

public abstract class OpenParameterDialog extends OpenFileDialog {
	public abstract void parameterFileLoaded(List<Parameter> parameters);

	@Override
	protected FileReader createReader() {
		return new ParameterReader();
	}

	@Override
	protected void onDataLoaded(FileReader reader) {
		parameterFileLoaded(((ParameterReader) reader).getParameters());
	}
}