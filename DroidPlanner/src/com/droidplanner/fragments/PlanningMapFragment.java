package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.droidplanner.R;
import com.droidplanner.R.string;
import com.droidplanner.drone.Drone;
import com.droidplanner.polygon.Polygon;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

@SuppressLint("UseSparseArrays")
public class PlanningMapFragment extends OfflineMapFragment implements
		OnMapLongClickListener, OnMarkerDragListener {
	
	public enum modes {
		MISSION, POLYGON;
	}
	
	private GoogleMap mMap;
	
	private HashMap<Integer, Marker> waypointMarkers = new HashMap<Integer, Marker>();
	private HashMap<Integer, Marker> polygonMarkers = new HashMap<Integer, Marker>();
	private Marker home;
	
	private OnMapInteractionListener mListener;

	public modes mode = modes.MISSION;

	public Polygon polygon;

	static final String homeMarkerTitle = "Home";

	public interface OnMapInteractionListener {

		public void onAddPoint(LatLng point);

		public void onMoveHome(LatLng coord);

		public void onMoveWaypoint(LatLng coord, int Number);

		public void onMovePolygonPoint(LatLng coord, int Number);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		mMap.setOnMarkerDragListener(this);
		mMap.setOnMapLongClickListener(this);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnMapInteractionListener) activity;
	}

	public void update(Drone drone, Polygon polygon) {
		mMap.clear();
		waypointMarkers.clear();
		polygonMarkers.clear();
		
		home = mMap.addMarker(getHomeIcon(drone));
		int i =0;
		for (MarkerOptions waypoint : getMissionMarkers(drone)) {
			waypointMarkers.put(i++,mMap.addMarker(waypoint));
		}
		mMap.addPolyline(getMissionPath(drone));

		i = 0;
		for (MarkerOptions point : getPolygonMarkers(polygon)) {
			polygonMarkers.put(i++,mMap.addMarker(point));
		}
		mMap.addPolyline(getPolygonPath(polygon));
	}

	@Override
	public void onMapLongClick(LatLng point) {
		mListener.onAddPoint(point);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		checkForHomeMarker(marker);
		checkForWaypointMarker(marker);
		checkForPolygonMarker(marker);
	}

	private void checkForHomeMarker(Marker marker) {
		if(home.equals(marker)){
			mListener.onMoveHome(marker.getPosition());
		}
	}

	private void checkForWaypointMarker(Marker marker) {
		if (waypointMarkers.containsValue(marker)) {
			int number = 0;
			for (HashMap.Entry<Integer, Marker> e : waypointMarkers.entrySet()) {
				if (marker.equals(e.getValue())) {
					number = e.getKey();
					break;
				}
			}
			mListener.onMoveWaypoint(marker.getPosition(), number);
		}
	}

	private void checkForPolygonMarker(Marker marker) {
		if (polygonMarkers.containsValue(marker)) {
			int number = 0;
			for (HashMap.Entry<Integer, Marker> e : polygonMarkers.entrySet()) {
				if (marker.equals(e.getValue())) {
					number = e.getKey();
					break;
				}
			}
			Log.d("MARK","move polygon");
			mListener.onMovePolygonPoint(marker.getPosition(), number);
		}
	}

	public LatLng getMyLocation() {
		if (mMap.getMyLocation() != null) {
			return new LatLng(mMap.getMyLocation().getLatitude(), mMap
					.getMyLocation().getLongitude());
		} else {
			return null;
		}
	}

	private MarkerOptions getHomeIcon(Drone drone) {
		return (new MarkerOptions()
				.position(drone.mission.getHome().coord)
				.snippet(
						String.format(Locale.ENGLISH, "%.2f",
								drone.mission.getHome().Height))
				.draggable(true)
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_menu_home))
				.title(homeMarkerTitle));
	}

	private List<MarkerOptions> getMissionMarkers(Drone drone) {
		int i = 1;
		List<MarkerOptions> MarkerList = new ArrayList<MarkerOptions>();
		for (waypoint point : drone.mission.getWaypoints()) {
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

	public List<MarkerOptions> getPolygonMarkers(Polygon poly) {
		int i = 1;
		List<MarkerOptions> MarkerList = new ArrayList<MarkerOptions>();
		for (LatLng point : poly.getWaypoints()) {
			MarkerList.add(new MarkerOptions()
					.position(point)
					.draggable(true)
					.title("Poly" + Integer.toString(i))
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
			i++;
		}
		return MarkerList;
	}

	private PolylineOptions getMissionPath(Drone drone) {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.YELLOW).width(3);
	
		flightPath.add(drone.mission.getHome().coord);
		for (waypoint point : drone.mission.getWaypoints()) {
			flightPath.add(point.coord);
		}
		return flightPath;
	}

	public PolylineOptions getPolygonPath(Polygon poly) {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.BLACK).width(2);
	
		for (LatLng point : poly.getWaypoints()) {
			flightPath.add(point);
		}
		if (poly.getWaypoints().size() > 2) {
			flightPath.add(poly.getWaypoints().get(0));
		}
	
		return flightPath;
	}

	public void setMode(modes mode) {
		this.mode = mode;
		switch (mode) {
		default:
		case MISSION:
			Toast.makeText(getActivity(), string.exiting_polygon_mode, Toast.LENGTH_SHORT)
			.show();			
			break;
		case POLYGON:
			Toast.makeText(getActivity(), string.entering_polygon_mode, Toast.LENGTH_SHORT)
			.show();
			break;
		}
	}
}
