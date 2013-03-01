package com.diydrones.droidplanner;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.WaypointMananger;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.diydrones.droidplanner.helpers.FileManager;
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

public class PlanningActivity extends android.support.v4.app.FragmentActivity
		implements OnNavigationListener, OnMapLongClickListener,
		OnMarkerDragListener {

	private GoogleMap mMap;

	public MissionManager mission;
	public Polygon polygon;
	public enum modes{
		MISSION,POLYGON;
	}
	public modes mode;
	
	TextView WaypointListNumber;
	private MenuItem connectButton;
	
	public MAVLinkClient MAVClient = new MAVLinkClient(this) {
		@Override
		public void notifyReceivedData(MAVLinkMessage m) {
			waypointMananger.processMessage(m);			
		}
		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(R.string.menu_disconnect));		
		}
		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(R.string.menu_connect));					
		}
	};
	
	
	WaypointMananger waypointMananger = new WaypointMananger(MAVClient) {
		@Override
		public void onWaypointsReceived(List<waypoint> waypoints) {
			if(waypoints!=null){
				Toast.makeText(getApplicationContext(), "Waypoints received from Drone", Toast.LENGTH_SHORT).show();
				Log.d("Mission", "Received all waypoints, size()="+waypoints.size());
				mission.setHome(waypoints.get(0));
				waypoints.remove(0);	// Remove Home waypoint
				mission.clearWaypoints();
				mission.addWaypoints(waypoints);
				updateMarkersAndPath();
				zoomToExtents();
			}
		}
		@Override
		public void onWriteWaypoints(msg_mission_ack msg) {
			Toast.makeText(getApplicationContext(), "Waypoints saved to Drone", Toast.LENGTH_SHORT).show();
		}
	};

	
	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up the action bar to show a dropdown list.
		setUpActionBar();

		setContentView(R.layout.planning);

		WaypointListNumber = (TextView) (findViewById(R.id.textViewWP));

		mission = new MissionManager();
		polygon = new Polygon();
		mode = modes.MISSION;
		setUpMapIfNeeded();
		
		updateMarkersAndPath();

		MAVClient.init();
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

	private void setUpActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.menu_dropdown,
				android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
		actionBar.setSelectedNavigationItem(0);
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

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		switch (itemPosition) {
		default:
		case 0: // Planning
			//startActivity(new Intent(this, PlanningActivity.class));
			return false;
		case 1: // HUD
			startActivity(new Intent(this, HUDActivity.class));
			return false;
		case 2: // Flight Data
			startActivity(new Intent(this, FightDataActivity.class));
			return false;
		case 3: // PID
			startActivity(new Intent(this, PIDActivity.class));
			return false;
		case 4: // Terminal
			startActivity(new Intent(this, TerminalActivity.class));
			return false;
		case 5: // GCP
			startActivity(new Intent(this, GCPActivity.class));
			return false;
		}
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
			OpenWaypointDialog();
			return true;
		case R.id.menu_save_file:
			menuSaveFile();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this,SettingsActivity.class));
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
			generatePolygon();
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

	private void setModeToPolygon() {
		mode = modes.POLYGON;
		Toast.makeText(this, R.string.entering_polygon_mode, Toast.LENGTH_SHORT).show();
		invalidateOptionsMenu();
	}

	private void setModeToMission() {
		mode = modes.MISSION;
		Toast.makeText(this, R.string.exiting_polygon_mode, Toast.LENGTH_SHORT).show();
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

	
	Double hatchAngle;
	Double hatchDistance = 100.0;
	private void generatePolygon() {
		hatchAngle = ((double) mMap.getCameraPosition().bearing + 90)%180;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Polygon Angle");

	    LayoutInflater inflater = getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.dialog_polygon, null));
	    
	    
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				})
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mission.addWaypoints(polygon.hatchfill(hatchAngle, hatchDistance,mission.getLastWaypoint().coord,mission.getDefaultAlt()));
				updateMarkersAndPath();
			}
		});
	    
	    AlertDialog dialog = builder.create();
	    dialog.show();
	    	    
	    SeekBar angleSeekBar = (SeekBar) dialog.findViewById(R.id.SeekBarAngle);
	    final TextView angleTextView = (TextView) dialog.findViewById(R.id.TextViewAngle);
	    angleTextView.setText(String.format(getResources().getString(R.string.dialog_polygon_hatch_angle)+"\t%3.0fº",hatchAngle));
	    angleSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {		
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				hatchAngle = (double) progress;
				angleTextView.setText(getResources().getString(R.string.dialog_polygon_hatch_angle)+"\t"+hatchAngle+"º");
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

	    SeekBar distanceSeekBar = (SeekBar) dialog.findViewById(R.id.seekBarDistance);
	    final TextView distanceTextView = (TextView) dialog.findViewById(R.id.textViewDistance);
	    distanceTextView.setText(getResources().getString(R.string.dialog_polygon_distance_between_lines)+"\t"+hatchDistance+"m");
	    distanceSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {		
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				hatchDistance = (double) progress;
				distanceTextView.setText(getResources().getString(R.string.dialog_polygon_distance_between_lines)+"\t"+hatchDistance+"m");
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

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
		if(mMap.getMyLocation() != null){
		return new LatLng(mMap.getMyLocation().getLatitude(), mMap
				.getMyLocation().getLongitude());
		}else {
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
		Log.d("Plan", "Zoom start");
		LatLngBounds bound = mission.getWaypointsBounds();
		Log.d("Plan", "Bound created");
		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, 480, 360,
				30));
		Log.d("Plan", "Zoom");
	}

	private void OpenWaypointDialog() {
		final String[] itemList = FileManager.loadWaypointFileList();
		if (itemList.length == 0) {
			Toast.makeText(getApplicationContext(), R.string.no_waypoint_files,
					Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(itemList, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (mission.openMission(FileManager.getWaypointsPath()+ itemList[which])) {
					zoomToExtents();
					Toast.makeText(getApplicationContext(), itemList[which],
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.error_when_opening_file,
							Toast.LENGTH_SHORT).show();
				}
				updateMarkersAndPath();
			}
		});
		dialog.create().show();
	}

}
