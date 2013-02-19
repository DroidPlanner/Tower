package com.diydrones.droidplanner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Environment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MissionManager {

	static final String homeMarkerTitle = "Home";

	private waypoint home;
	private Double defaultAlt;
	private List<waypoint> waypoints;

	public MissionManager() {
		home = new waypoint(new LatLng(0, 0), (Double) 0.0);
		waypoints = new ArrayList<waypoint>();
		setDefaultAlt(100.0);
	}

	public MarkerOptions getHomeIcon() {
		return (new MarkerOptions()
				.position(home.coord)
				.snippet(String.format(Locale.ENGLISH, "%.2f", home.Height))
				.draggable(true)
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_menu_home))
				.title(homeMarkerTitle));
	}

	public PolylineOptions getFlightPath() {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.YELLOW).width(3);

		flightPath.add(home.coord);
		for (waypoint point : waypoints) {
			flightPath.add(point.coord);
		}
		return flightPath;
	}

	public List<MarkerOptions> getWaypointMarkers() {
		int i = 1;
		List<MarkerOptions> MarkerList = new ArrayList<MarkerOptions>();
		for (waypoint point : waypoints) {
			MarkerList
					.add(new MarkerOptions()
							.position(point.coord)
							.draggable(true)
							.title("WP" + Integer.toString(i))
							.snippet(
									String.format(Locale.ENGLISH, "%.2f",
											point.Height)));
			i++;
		}
		return MarkerList;
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

	public LatLngBounds getHomeAndWaypointsBounds(LatLng myLocation) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(home.coord);
		if (waypoints.isEmpty() && (myLocation!= null)) {
			builder.include(myLocation);
		} else {
			for (waypoint w : waypoints) {
				builder.include(w.coord);
			}
		}
		return builder.build();
	}

	public LatLngBounds getWaypointsBounds() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(home.coord);
		for (waypoint w : waypoints) {
			builder.include(w.coord);
		}
		return builder.build();
	}

	public boolean isHomeMarker(Marker marker) {
		return marker.getTitle().equals(homeMarkerTitle);
	}

	public boolean isWaypointMarker(Marker marker) {
		return marker.getTitle().contains("WP");
	}

	public void setHomeToMarker(Marker marker) {
		home.coord = marker.getPosition();
		home.Height = Double.parseDouble(marker.getSnippet().replace(",", "."));
	}

	public void setWaypointToMarker(Marker marker) {
		int WPnumber = Integer.parseInt(marker.getTitle().replace("WP", "")) - 1;
		waypoints.get(WPnumber).coord = marker.getPosition();
		waypoints.get(WPnumber).Height = Double.parseDouble(marker.getSnippet()
				.replace(",", "."));
	}

	@SuppressLint("DefaultLocale")
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
	
	public waypoint getLastWaypoint(){
		if(waypoints.size()>0)
			return waypoints.get(waypoints.size()-1);
		else
			return home;
	}

	public void setHome(waypoint home) {
		this.home = home;
	}

}
