package com.diydrones.droidplanner;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.WaypointMananger;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.diydrones.droidplanner.helpers.OpenMissionDialog;
import com.diydrones.droidplanner.helpers.PolygonDialog;
import com.diydrones.droidplanner.helpers.TTS;
import com.diydrones.droidplanner.service.MAVLinkClient;
import com.diydrones.droidplanner.waypoints.MissionManager;
import com.diydrones.droidplanner.waypoints.Polygon;
import com.diydrones.droidplanner.waypoints.waypoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlanningActivity extends Activity implements
		OnMapLongClickListener, OnMarkerDragListener {

	private GoogleMap mMap;

	public MissionManager mission;
	public Polygon polygon;

	public enum modes {
		MISSION, POLYGON;
	}

	public modes mode;

	TextView WaypointListNumber;
	private MenuItem connectButton;

	TTS tts;

	@Override
	int getNavigationItem() {
		return 0;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.planning);
	
		WaypointListNumber = (TextView) (findViewById(R.id.textViewWP));
	
		mission = new MissionManager();
		polygon = new Polygon();
		mode = modes.MISSION;
		setUpMapIfNeeded();
	
		updateMarkersAndPath();
	
	
		tts = new TTS(this);
	
		MAVClient.init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MAVClient.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		switch (mode) {
		default:
		case MISSION:
			getMenuInflater().inflate(R.menu.menu_planning, menu);
			break;
		case POLYGON:
			getMenuInflater().inflate(R.menu.menu_planning_polygon, menu);
			break;
		}
		connectButton = menu.findItem(R.id.menu_connect);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_clear_wp:
			clearWaypointsAndUpdate();
			return true;
		case R.id.menu_load_from_apm:
			waypointMananger.getWaypoints();
			return true;
		case R.id.menu_send_to_apm:
			List<waypoint> data = new ArrayList<waypoint>();
			data.add(mission.getHome());
			data.addAll(mission.getWaypoints());
			waypointMananger.writeWaypoints(data);
			return true;
		case R.id.menu_open_file:
			openMissionFile();
			return true;
		case R.id.menu_save_file:
			menuSaveFile();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();
			return true;
		case R.id.menu_zoom:
			zoomToExtents();
			return true;
		case R.id.menu_default_alt:
			changeDefaultAlt();
			return true;
		case R.id.menu_polygon:
			setModeToPolygon();
			return true;
		case R.id.menu_generate_polygon:
			openPolygonGenerateDialog();
			return true;
		case R.id.menu_clear_polygon:
			polygon.clearPolygon();
			updateMarkersAndPath();
			return true;
		case R.id.menu_finish_polygon:
			setModeToMission();
			updateMarkersAndPath();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void openMissionFile() {
		OpenMissionDialog missionDialog = new OpenMissionDialog() {				
			@Override
			public void waypointFileLoaded(boolean isFileOpen) {
				if(isFileOpen){
					zoomToExtents();
				}
				updateMarkersAndPath();
			}
		};
		missionDialog.OpenWaypointDialog(mission, this);
	}

	public void openPolygonGenerateDialog() {
		double defaultHatchAngle = ((double) mMap.getCameraPosition().bearing + 90) % 180;
		PolygonDialog polygonDialog = new PolygonDialog() {
			@Override
			public void onPolygonGenerated(List<waypoint> list) {
				mission.addWaypoints(list);
				updateMarkersAndPath();
			}
		};
		polygonDialog.generatePolygon(defaultHatchAngle, 50.0, polygon, mission.getLastWaypoint().coord, mission.getDefaultAlt(), this);	
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		mMap.setOnMarkerDragListener(this);

		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);

		mMap.setOnMapLongClickListener(this);
		updateMarkersAndPath();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_VIEW.equals(action) && type != null) {
			Toast.makeText(this, intent.getData().getPath(), Toast.LENGTH_LONG)
					.show();
			mission.openMission(intent.getData().getPath());
			updateMarkersAndPath();
			Log.d("Plan", "loaded mission");
			zoomToExtentsFixed();
		}

	}

	@Override
	public void onMapLongClick(LatLng point) {

		switch (mode) {
		default:
		case MISSION:
			mission.addWaypoint(point);
			break;
		case POLYGON:
			polygon.addWaypoint(point);
			break;
		}
		updateMarkersAndPath();
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		if (mission.isHomeMarker(marker)) {
			mission.setHomeToMarker(marker);
			updateMarkersAndPath();
			return;
		} else if (mission.isWaypointMarker(marker)) {
			mission.setWaypointToMarker(marker);
			updateMarkersAndPath();
			return;
		} else if (polygon.isPolygonMarker(marker)) {
			polygon.setWaypointToMarker(marker);
			updateMarkersAndPath();
			return;
		}
	}

	private void updateMarkersAndPath() {
		mMap.clear();
		mMap.addMarker(mission.getHomeIcon());
		for (MarkerOptions waypoint : mission.getWaypointMarkers()) {
			mMap.addMarker(waypoint);
		}
		mMap.addPolyline(mission.getFlightPath());

		WaypointListNumber.setText(mission.getWaypointData());

		for (MarkerOptions point : polygon.getWaypointMarkers()) {
			mMap.addMarker(point);
		}
		mMap.addPolyline(polygon.getFlightPath());
	}

	private void clearWaypointsAndUpdate() {
		mission.clearWaypoints();
		mMap.clear();
		updateMarkersAndPath();
	}

	private void setModeToPolygon() {
		mode = modes.POLYGON;
		Toast.makeText(this, R.string.entering_polygon_mode, Toast.LENGTH_SHORT)
				.show();
		invalidateOptionsMenu();
	}

	private void setModeToMission() {
		mode = modes.MISSION;
		Toast.makeText(this, R.string.exiting_polygon_mode, Toast.LENGTH_SHORT)
				.show();
		invalidateOptionsMenu();
	}

	private void changeDefaultAlt() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Default Altitude");

		final NumberPicker numb3rs = new NumberPicker(this);
		numb3rs.setMaxValue(1000);
		numb3rs.setMinValue(0);
		numb3rs.setValue((mission.getDefaultAlt().intValue()));
		builder.setView(numb3rs);

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mission.setDefaultAlt((double) numb3rs.getValue());
				updateMarkersAndPath();
			}
		});
		builder.create().show();
	}

	
	private void menuSaveFile() {
		if (mission.saveWaypoints()) {
			Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, R.string.error_when_saving, Toast.LENGTH_SHORT)
					.show();
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

	public void zoomToExtents() {
		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
				mission.getHomeAndWaypointsBounds(getMyLocation()), 30));
	}

	/**
	 * Zoom to the extent of the waypoints should be used when the maps has not
	 * undergone the layout phase Assumes a map size of 480x360 px
	 */
	public void zoomToExtentsFixed() {
		LatLngBounds bound = mission.getWaypointsBounds();
		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, 480, 360,
				30));
	}

	public MAVLinkClient MAVClient = new MAVLinkClient(this) {
		@Override
		public void notifyReceivedData(MAVLinkMessage m) {
			waypointMananger.processMessage(m);
		}
	
		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
			tts.speak("Connected");
		}
	
		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
			tts.speak("Disconnected");
		}
	};

	WaypointMananger waypointMananger = new WaypointMananger(MAVClient) {
		@Override
		public void onWaypointsReceived(List<waypoint> waypoints) {
			if (waypoints != null) {
				Toast.makeText(getApplicationContext(),
						"Waypoints received from Drone", Toast.LENGTH_SHORT)
						.show();
				Log.d("Mission",
						"Received all waypoints, size()=" + waypoints.size());
				tts.speak("Received waypoints from Drone");
				mission.setHome(waypoints.get(0));
				waypoints.remove(0); // Remove Home waypoint
				mission.clearWaypoints();
				mission.addWaypoints(waypoints);
				updateMarkersAndPath();
				zoomToExtents();
			}
		}
	
		@Override
		public void onWriteWaypoints(msg_mission_ack msg) {
			Toast.makeText(getApplicationContext(), "Waypoints saved to Drone",
					Toast.LENGTH_SHORT).show();
			tts.speak("Waypoints saved to Drone");
		}
	};

}
