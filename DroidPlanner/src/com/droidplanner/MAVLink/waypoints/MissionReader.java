package com.droidplanner.MAVLink.waypoints;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.MAVLink.waypoint;
import com.droidplanner.helpers.FileManager;

public class MissionReader {
	private waypoint home;
	private List<waypoint> waypoints;

	public MissionReader() {
		this.waypoints = new ArrayList<waypoint>();
	}

	public boolean openMission(String itemList) {
		if (!FileManager.isExternalStorageAvaliable()) {
			return false;
		}
		try {
			FileInputStream in = new FileInputStream(itemList);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));

			if (!isWaypointFile(reader)) {
				in.close();
				return false;
			}
			parseHomeLine(reader);
			parseWaypointLines(reader);

			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public waypoint getHome() {
		return home;
	}

	public List<waypoint> getWaypoints() {
		return waypoints;
	}

	private void parseWaypointLines(BufferedReader reader) throws IOException {
		String line;
		waypoints.clear();
		while ((line = reader.readLine()) != null) {
			String[] RowData = line.split("\t");
			waypoints.add(new waypoint(Double.valueOf(RowData[8]), Double
					.valueOf(RowData[9]), Double.valueOf(RowData[10])));
		}

	}

	private void parseHomeLine(BufferedReader reader) throws IOException {
		String[] RowData1 = reader.readLine().split("\t");
		home = new waypoint(Double.valueOf(RowData1[8]),
				Double.valueOf(RowData1[9]), Double.valueOf(RowData1[10]));
	}

	private static boolean isWaypointFile(BufferedReader reader)
			throws IOException {
		return reader.readLine().contains("QGC WPL 110");
	}
}
