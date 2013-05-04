package com.droidplanner.MAVLink.parameters;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.FileManager;

public class ParameterReader {
	private List<Parameter> parameters;

	public ParameterReader() {
		this.parameters = new ArrayList<Parameter>();
	}

	public boolean openFile(String itemList) {
		if (!FileManager.isExternalStorageAvaliable()) {
			return false;
		}
		try {
			FileInputStream in = new FileInputStream(itemList);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));

			if (!isParameterFile(reader)) {
				in.close();
				return false;
			}
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
			}
		}
	}

	private void parseLine(String line) throws Exception {
		String[] RowData = splitLine(line);

		String name = RowData[0];
		Double value = Double.valueOf(RowData[1]);

		isParamNameAnExeption(name);

		parameters.add(new Parameter(name, value));
	}

	private String[] splitLine(String line) throws Exception {
		String[] RowData = line.split(",");
		if (RowData.length != 2) {
			throw new Exception("Invalid Length");
		}
		return RowData;
	}

	private void isParamNameAnExeption(String name) throws Exception {
		if (name == "SYSID_SW_MREV")
			throw new Exception("ExludedName");
		if (name == "WP_TOTAL")
			throw new Exception("ExludedName");
		if (name == "CMD_TOTAL")
			throw new Exception("ExludedName");
		if (name == "FENCE_TOTAL")
			throw new Exception("ExludedName");
		if (name == "SYS_NUM_RESETS")
			throw new Exception("ExludedName");
		if (name == "ARSPD_OFFSET")
			throw new Exception("ExludedName");
		if (name == "GND_ABS_PRESS")
			throw new Exception("ExludedName");
		if (name == "GND_TEMP")
			throw new Exception("ExludedName");
		if (name == "CMD_INDEX")
			throw new Exception("ExludedName");
		if (name == "LOG_LASTFILE")
			throw new Exception("ExludedName");
		if (name == "FORMAT_VERSION")
			throw new Exception("ExludedName");
		return;
	}

	private static boolean isParameterFile(BufferedReader reader)
			throws IOException {
		return reader.readLine().contains("#NOTE");
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

}
