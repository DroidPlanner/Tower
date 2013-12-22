package com.droidplanner.file.IO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.dialogs.openfile.OpenFileDialog.FileReader;
import com.droidplanner.drone.variables.Home;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.FileList;
import com.droidplanner.file.FileManager;

public class MissionReader implements FileReader {
	private Home home;
	private List<SpatialCoordItem> waypoints;

	public MissionReader() {
		this.waypoints = new ArrayList<SpatialCoordItem>();
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

	public List<SpatialCoordItem> getWaypoints() {
		return waypoints;
	}

	private void parseWaypointLines(BufferedReader reader) throws IOException {
		String line;
		waypoints.clear();
		while ((line = reader.readLine()) != null) {
			throw new IllegalArgumentException("NOT implemented"); //TODO implement this
			/*
			String[] RowData = line.split("\t");
			Waypoint wp = new Waypoint(Double.valueOf(RowData[8]),
					Double.valueOf(RowData[9]), Double.valueOf(RowData[10]));
			wp.setNumber(Integer.valueOf(RowData[0]));
			wp.setFrame(Integer.valueOf(RowData[2]));
			wp.setCmd(ApmCommands.getCmd(Integer.valueOf(RowData[3])));
			wp.setParameters(Float.valueOf(RowData[4]),
					Float.valueOf(RowData[5]), Float.valueOf(RowData[6]),
					Float.valueOf(RowData[7]));
			wp.setAutoContinue(Integer.valueOf(RowData[11]));
			waypoints.add(wp);
			*/
		}

	}

	private void parseHomeLine(BufferedReader reader) throws IOException {
		throw new IllegalArgumentException("NOT implemented"); //TODO implement this
		/*
		String[] RowData = reader.readLine().split("\t");
		home = new Home(Double.valueOf(RowData[8]), Double.valueOf(RowData[9]),
				Double.valueOf(RowData[10]));
		home.setNumber(Integer.valueOf(RowData[0]));
		home.setFrame(Integer.valueOf(RowData[2]));
		home.setCmd(ApmCommands.getCmd(Integer.valueOf(RowData[3])));
		home.setParameters(Float.valueOf(RowData[4]),
				Float.valueOf(RowData[5]), Float.valueOf(RowData[6]),
				Float.valueOf(RowData[7]));
		home.setAutoContinue(Integer.valueOf(RowData[11]));
		*/
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
