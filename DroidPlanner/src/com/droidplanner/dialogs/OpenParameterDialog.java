package com.droidplanner.dialogs;

import java.util.List;

import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterReader;

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