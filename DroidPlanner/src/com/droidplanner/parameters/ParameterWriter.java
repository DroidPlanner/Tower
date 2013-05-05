package com.droidplanner.parameters;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.droidplanner.helpers.file.FileManager;
import com.droidplanner.helpers.file.FileStream;

public class ParameterWriter {
	private List<Parameter> parameterList;

	public ParameterWriter(List<Parameter> param) {
		this.parameterList = param;
	}

	public boolean saveParametersToFile() {
		try {
			if (!FileManager.isExternalStorageAvaliable()) {
				return false;
			}
			FileOutputStream out = FileStream.getParameterFileStream();

			writeFirstLine(out);

			writeWaypointsLines(out);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeFirstLine(FileOutputStream out) throws IOException {
		out.write((new String("#NOTE: " + FileManager.getTimeStamp()+"\n")
				.getBytes()));
	}

	private void writeWaypointsLines(FileOutputStream out) throws IOException {
		for (Parameter param : parameterList) {
			out.write(String.format(Locale.ENGLISH, "%s , %f\n", param.name,
					param.value).getBytes());
		}
	}
}
