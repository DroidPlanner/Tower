package com.diydrones.droidplanner.waypoints;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Environment;

import com.diydrones.droidplanner.helpers.FileManager;
import com.google.android.gms.maps.model.LatLng;

public class MissionManager {
	private waypoint home;
	private Double defaultAlt;
	private List<waypoint> waypoints;

	public MissionManager() {
		home = new waypoint(new LatLng(0, 0), (Double) 0.0);
		waypoints = new ArrayList<waypoint>();
		setDefaultAlt(100.0);
	}


	public void addWaypoints(List<waypoint> points) {
		waypoints.addAll(points);
	}

	public void addWaypoint(Double Lat, Double Lng, Double h) {
		waypoints.add(new waypoint(Lat, Lng, h));
	}

	public void addWaypoint(LatLng coord, Double h) {
		waypoints.add(new waypoint(coord, h));
	}

	public void addWaypoint(LatLng coord) {
		addWaypoint(coord, getDefaultAlt());
	}

	public void clearWaypoints() {
		waypoints.clear();
	}

	public boolean openMission(String itemList) {
		if (!isExternalStorageWritable()) {
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

	private boolean isWaypointFile(BufferedReader reader) throws IOException {
		return reader.readLine().contains("QGC WPL 110");
	}

	private void parseWaypointLines(BufferedReader reader) throws IOException {
		String line;
		clearWaypoints();
		while ((line = reader.readLine()) != null) {
			String[] RowData = line.split("\t");
			addWaypoint(Double.valueOf(RowData[8]), Double.valueOf(RowData[9]),
					Double.valueOf(RowData[10]));
		}

	}

	private void parseHomeLine(BufferedReader reader) throws IOException {
		String[] RowData1 = reader.readLine().split("\t");
		home = new waypoint(Double.valueOf(RowData1[8]),
				Double.valueOf(RowData1[9]), Double.valueOf(RowData1[10]));
	}

	public boolean saveWaypoints() {
		try {
			if (!isExternalStorageWritable()) {
				return false;
			}
			FileOutputStream out = FileManager.getWaypointFileStream();

			writeFirstLine(out);

			writeWaypointsLines(out);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeWaypointsLines(FileOutputStream out) throws IOException {
		for (int i = 0; i < waypoints.size(); i++) {
			out.write(String
					.format(Locale.ENGLISH,
							"%d\t0\t%d\t%d\t0.000000\t0.000000\t0.000000\t0.000000\t%f\t%f\t%f\t1\n",
							i + 1,
							0, // TODO Implement Relative Altitude
							16,// TODO Implement other modes (16 == auto?)
							waypoints.get(i).coord.latitude,
							waypoints.get(i).coord.longitude,
							waypoints.get(i).Height).getBytes());
		}
	}

	private void writeFirstLine(FileOutputStream out) throws IOException {
		out.write(String.format(Locale.ENGLISH,
				"QGC WPL 110\n0\t1\t0\t16\t0\t0\t0\t0\t%f\t%f\t%f\t1\n",
				home.coord.latitude, home.coord.longitude, home.Height)
				.getBytes());

	}

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
	
	public String getWaypointData() {
		String waypointData = String.format(Locale.ENGLISH, "Home\t%2.0f\n",
				home.Height);
		waypointData += String.format("Def:\t%2.0f\n", getDefaultAlt());

		int i = 1;
		for (waypoint point : waypoints) {
			waypointData += String.format(Locale.ENGLISH, "WP%02d \t%2.0f\n",
					i++, point.Height);
		}
		return waypointData;
	}

	public List<waypoint> getWaypoints() {
		return waypoints;
	}

	public Double getDefaultAlt() {
		return defaultAlt;
	}

	public void setDefaultAlt(Double defaultAlt) {
		this.defaultAlt = defaultAlt;
	}

	public waypoint getHome() {
		return home;
	}

	public waypoint getLastWaypoint() {
		if (waypoints.size() > 0)
			return waypoints.get(waypoints.size() - 1);
		else
			return home;
	}

	public void setHome(waypoint home) {
		this.home = home;
	}

	public void moveWaypoint(LatLng coord, double height, int number) {
		waypoints.set(number, new waypoint(coord, height));
	}


	public List<LatLng> getAllCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (waypoint point : waypoints) {
			result.add(point.coord);
		}
		result.add(home.coord);
		return result;
	}

}
