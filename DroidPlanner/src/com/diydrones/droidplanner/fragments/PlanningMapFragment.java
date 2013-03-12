package com.diydrones.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diydrones.droidplanner.R;
import com.diydrones.droidplanner.waypoints.MissionManager;
import com.diydrones.droidplanner.waypoints.Polygon;
import com.diydrones.droidplanner.waypoints.waypoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class PlanningMapFragment extends MapFragment implements
		OnMapLongClickListener, OnMarkerDragListener {
	private GoogleMap mMap;

	private OnMapInteractionListener mListener;

	static final String homeMarkerTitle = "Home";

	public interface OnMapInteractionListener {
		public void onAddWaypoint(LatLng point);

		public void onMoveHome(LatLng coord, double height);

		public void onMoveWaypoint(LatLng coord, double height, int Number);

		public void onMovePolygonPoint(LatLng coord, int Number);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		mMap.setMyLocationEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		mMap.setOnMarkerDragListener(this);

		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);

		mMap.setOnMapLongClickListener(this);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnMapInteractionListener) activity;
	}

	public void update(MissionManager mission, Polygon polygon) {
		mMap.clear();
		mMap.addMarker(getHomeIcon(mission));
		for (MarkerOptions waypoint : getMissionMarkers(mission)) {
			mMap.addMarker(waypoint);
		}
		mMap.addPolyline(getMissionPath(mission));

		for (MarkerOptions point : getPolygonMarkers(polygon)) {
			mMap.addMarker(point);
		}
		mMap.addPolyline(getPolygonPath(polygon));
	}

	public void zoomToExtents(MissionManager mission) {
		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
				getHomeAndWaypointsBounds(mission), 30));
	}

	/**
	 * Zoom to the extent of the waypoints should be used when the maps has not
	 * undergone the layout phase Assumes a map size of 480x360 px
	 */
	public void zoomToExtentsFixed(MissionManager mission) {
		LatLngBounds bound = getWaypointsBounds(mission);
		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, 480, 360,
				30));
	}

	private MarkerOptions getHomeIcon(MissionManager mission) {
		return (new MarkerOptions()
				.position(mission.getHome().coord)
				.snippet(
						String.format(Locale.ENGLISH, "%.2f",
								mission.getHome().Height))
				.draggable(true)
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_menu_home))
				.title(homeMarkerTitle));
	}

	private PolylineOptions getMissionPath(MissionManager mission) {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.YELLOW).width(3);

		flightPath.add(mission.getHome().coord);
		for (waypoint point : mission.getWaypoints()) {
			flightPath.add(point.coord);
		}
		return flightPath;
	}

	private List<MarkerOptions> getMissionMarkers(MissionManager mission) {
		int i = 1;
		List<MarkerOptions> MarkerList = new ArrayList<MarkerOptions>();
		for (waypoint point : mission.getWaypoints()) {
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

	private LatLngBounds getHomeAndWaypointsBounds(MissionManager mission) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(mission.getHome().coord);
		LatLng myLocation = getMyLocation();
		List<waypoint> waypoints = mission.getWaypoints();
		if (waypoints.isEmpty() && (myLocation != null)) {
			builder.include(myLocation);
		} else {
			for (waypoint w : waypoints) {
				builder.include(w.coord);
			}
		}
		return builder.build();
	}

	private LatLngBounds getWaypointsBounds(MissionManager mission) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(mission.getHome().coord);
		for (waypoint w : mission.getWaypoints()) {
			builder.include(w.coord);
		}
		return builder.build();
	}

	@Override
	public void onMapLongClick(LatLng point) {
		mListener.onAddWaypoint(point);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		LatLng coord = marker.getPosition();
		if (isHomeMarker(marker)) {
			double Height = Double.parseDouble(marker.getSnippet().replace(",",
					"."));
			mListener.onMoveHome(coord, Height);
			return;
		} else if (isWaypointMarker(marker)) {
			int Number = Integer.parseInt(marker.getTitle().replace("WP", "")) - 1;
			double Height = Double.parseDouble(marker.getSnippet().replace(",",
					"."));
			mListener.onMoveWaypoint(coord, Height, Number);
			return;
		} else if (isPolygonMarker(marker)) {
			int Number = Integer
					.parseInt(marker.getTitle().replace("Poly", "")) - 1;
			mListener.onMovePolygonPoint(coord, Number);
			return;
		}
	}

	private boolean isHomeMarker(Marker marker) {
		return marker.getTitle().equals(homeMarkerTitle);
	}

	private boolean isWaypointMarker(Marker marker) {
		return marker.getTitle().contains("WP");
	}

	private boolean isPolygonMarker(Marker marker) {
		return marker.getTitle().contains("Poly");
	}

	public double getMapRotation() {
		return mMap.getCameraPosition().bearing;
	}

	public LatLng getMyLocation() {
		if (mMap.getMyLocation() != null) {
			return new LatLng(mMap.getMyLocation().getLatitude(), mMap
					.getMyLocation().getLongitude());
		} else {
			return null;
		}
	}
}
