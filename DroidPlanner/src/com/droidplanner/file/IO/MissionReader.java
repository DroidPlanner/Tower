package com.droidplanner.file.IO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.dialogs.OpenFileDialog.FileReader;
import com.droidplanner.drone.variables.Home;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.FileList;
import com.droidplanner.file.FileManager;

public class MissionReader implements FileReader {
	private Home home;
	private List<waypoint> waypoints;

	public MissionReader() {
		this.waypoints = new ArrayList<waypoint>();
	}

	public boolean openMission(String file) {
		if (!FileManager.isExternalStorageAvaliable()) {
			return false;
		}
		try {
			FileInputStream in = new FileInputStream(file);
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

	public Home getHome() {
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
			waypoint wp = new waypoint(Double.valueOf(RowData[8]),
					Double.valueOf(RowData[9]), Double.valueOf(RowData[10]),Integer.valueOf(RowData[2]));
			wp.setNumber(Integer.valueOf(RowData[0]));
			wp.setCmd(ApmCommands.getCmd(Integer.valueOf(RowData[3])));
			wp.setParameters(Float.valueOf(RowData[4]),
					Float.valueOf(RowData[5]), Float.valueOf(RowData[6]),
					Float.valueOf(RowData[7]));
			wp.setAutoContinue(Integer.valueOf(RowData[11]));
			waypoints.add(wp);
		}

	}

	private void parseHomeLine(BufferedReader reader) throws IOException {
		String[] RowData = reader.readLine().split("\t");
		home = new Home(Double.valueOf(RowData[8]),
				Double.valueOf(RowData[9]), Double.valueOf(RowData[10]),Integer.valueOf(RowData[2]));
		home.setNumber(Integer.valueOf(RowData[0]));
		home.setCmd(ApmCommands.getCmd(Integer.valueOf(RowData[3])));
		home.setParameters(Float.valueOf(RowData[4]),
				Float.valueOf(RowData[5]), Float.valueOf(RowData[6]),
				Float.valueOf(RowData[7]));
		home.setAutoContinue(Integer.valueOf(RowData[11]));
	}

	private static boolean isWaypointFile(BufferedReader reader)
			throws IOException {
		return reader.readLine().contains("QGC WPL 110");
	}

	@Override
	public String getPath() {
		return DirectoryPath.getWaypointsPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getWaypointFileList();
	}

	@Override
	public boolean openFile(String file) {
		return openMission(file);
	}
}
