package com.droidplanner.file.IO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.droidplanner.drone.variables.mission.waypoints.GenericWaypoint;
import com.droidplanner.file.FileManager;
import com.droidplanner.file.FileStream;

public class MissionWriter {
	private GenericWaypoint home;
	private List<GenericWaypoint> waypoints;
	private String name = "";

	public MissionWriter(GenericWaypoint home, List<GenericWaypoint> waypoints, String name) {
		this.home = home;
		this.waypoints = waypoints;
		this.name = name;
	}

	public MissionWriter(GenericWaypoint home, List<GenericWaypoint> waypoints) {
		this(home, waypoints, "waypoints");
	}

	public boolean saveWaypoints() {
		try {
			if (!FileManager.isExternalStorageAvaliable()) {
				return false;
			}
			FileOutputStream out = FileStream.getWaypointFileStream(name);

			writeFirstLine(out);
			writeHomeLine(out);
			writeWaypointsLines(out);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeFirstLine(FileOutputStream out) throws IOException {
		out.write(String.format(Locale.ENGLISH, "QGC WPL 110\n").getBytes());
	}

	private void writeHomeLine(FileOutputStream out) throws IOException {
		throw new IllegalArgumentException("NOT implemented"); //TODO implement this
		/*
		out.write(String.format(Locale.ENGLISH,
				"0\t1\t0\t16\t0\t0\t0\t0\t%f\t%f\t%f\t1\n",
				home.getCoord().latitude, home.getCoord().longitude,
				home.getHeight()).getBytes());
				*/
	}

	private void writeWaypointsLines(FileOutputStream out) throws IOException {

		throw new IllegalArgumentException("NOT implemented");	//TODO implement this
		/*
		for (int i = 0; i < waypoints.size(); i++) {
			Waypoint wp = waypoints.get(i);
			out.write(String.format(Locale.ENGLISH,
					"%d\t0\t%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%d\n", i + 1,
					wp.getFrame(), wp.getCmd().getType(), wp.getParam1(),
					wp.getParam2(), wp.getParam3(), wp.getParam4(),
					wp.getCoord().latitude, wp.getCoord().longitude,
					wp.getHeight(), wp.getAutoContinue()).getBytes());
		}
		*/		
	}
}
