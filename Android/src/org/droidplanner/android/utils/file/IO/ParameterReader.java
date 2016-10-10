package org.droidplanner.android.utils.file.IO;

import com.o3dr.services.android.lib.drone.property.Parameter;

import org.droidplanner.android.utils.file.FileStream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ParameterReader  {
	private List<Parameter> parameters;

	public ParameterReader() {
		this.parameters = new ArrayList<Parameter>();
	}

	public boolean openFile(String itemList) {
		if (!FileStream.isExternalStorageAvailable()) {
			return false;
		}
		try {
			FileInputStream in = new FileInputStream(itemList);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			parseWaypointLines(reader);

			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void parseWaypointLines(BufferedReader reader) throws IOException {
		String line;
		parameters.clear();
		while ((line = reader.readLine()) != null) {
			try {
				parseLine(line);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void parseLine(String line) throws Exception {
		String[] RowData = splitLine(line);

		String name = RowData[0];
		Double value = Double.valueOf(RowData[1]);

		parameters.add(new Parameter(name, value, 0));
	}

	private String[] splitLine(String line) throws Exception {
		String[] RowData = line.split(",");
		if (RowData.length != 2) {
			throw new Exception("Invalid Length");
		}
		RowData[0] = RowData[0].trim();
		return RowData;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

}
