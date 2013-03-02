package com.diydrones.droidplanner;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.diydrones.droidplanner.dialogs.OpenGcpFileDialog;
import com.diydrones.droidplanner.helpers.KmlParser;
import com.diydrones.droidplanner.helpers.KmlParser.waypoint;
import com.diydrones.droidplanner.helpers.mapHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class GCPActivity extends Activity implements OnMarkerClickListener {
	private GoogleMap mMap;

	public List<waypoint> WPlist;

	@Override
	int getNavigationItem() {
		return 5;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.gcp);
	
		WPlist = new ArrayList<waypoint>();
	
		setUpMapIfNeeded();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_gcp, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_clear:
			clearWaypointsAndUpdate();
			return true;
		case R.id.menu_open_kmz:
			openGcpFile();
			return true;
		case R.id.menu_zoom:
			zoomToExtents();
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public void openGcpFile() {
		OpenGcpFileDialog dialog = new OpenGcpFileDialog() {			
			@Override
			public void onGcpFileLoaded(List<waypoint> list) {
				if(list!=null)
				WPlist.clear();
				WPlist.addAll(list);
				updateMarkers();
				zoomToExtents();
			}
		};		
		dialog.openGCPDialog(this);
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

		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);

		mMap.setOnMarkerClickListener(this);

		updateMarkers();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_VIEW.equals(action) && type != null) {
			Toast.makeText(this, intent.getData().getPath(), Toast.LENGTH_LONG)
					.show();
			KmlParser parser = (new KmlParser());
			boolean fileIsOpen = parser.openGCPFile(intent.getData().getPath());
			if(fileIsOpen){
					WPlist.clear();
					WPlist.addAll(parser.WPlist);
					updateMarkers();
					zoomToExtentsFixed();
			}
		}

	}


	@Override
	public boolean onMarkerClick(Marker marker) {
		int i = Integer.parseInt(marker.getTitle()) - 1;
		WPlist.get(i).set = !WPlist.get(i).set;
		updateMarkers();
		return true;
	}

	private void updateMarkers() {
		mMap.clear();
		mapHelper.setupMapOverlay(
				mMap,
				PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
						"pref_advanced_use_offline_maps", false));
		int i = 1;
		for (waypoint point : WPlist) {
			mapHelper.addGcpMarkerToMap(mMap, i, point.coord, point.set);
			i++;
		}
	}

	private void clearWaypointsAndUpdate() {
		WPlist.clear();
		updateMarkers();
	}

	public void zoomToExtents() {
		if (!WPlist.isEmpty()) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (waypoint point : WPlist) {
				builder.include(point.coord);
			}
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 30));
		}
	}

	/**
	 * Zoom to the extent of the waypoints should be used when the maps has not
	 * undergone the layout phase Assumes a map size of 480x360 px
	 */
	public void zoomToExtentsFixed() {
		if (!WPlist.isEmpty()) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (waypoint point : WPlist) {
				builder.include(point.coord);
			}
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 480, 360, 30));
		}
	}
}
