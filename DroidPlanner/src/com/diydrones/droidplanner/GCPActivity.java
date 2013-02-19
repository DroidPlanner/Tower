package com.diydrones.droidplanner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.diydrones.droidplanner.KmlParser.waypoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GCPActivity extends android.support.v4.app.FragmentActivity
		implements OnNavigationListener, OnMarkerClickListener {
	private GoogleMap mMap;

	private List<waypoint> WPlist;

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

		setContentView(R.layout.gcp);

		WPlist = new ArrayList<waypoint>();

		setUpMapIfNeeded();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_gcp, menu);

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
		actionBar.setSelectedNavigationItem(5);
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
			openGCPFile(intent.getData().getPath());
			zoomToExtentsFixed();
		}

	}

	private void updateMarkers() {
		int i = 1;
		mMap.clear();
		for (waypoint point : WPlist) {
			if (point.set) {
				mMap.addMarker(new MarkerOptions()
						.position(point.coord)
						.title(String.valueOf(i))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.placemark_circle_blue))
						.anchor((float) 0.5, (float) 0.5));
			} else {
				mMap.addMarker(new MarkerOptions()
						.position(point.coord)
						.title(String.valueOf(i))
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.placemark_circle_red))
						.anchor((float) 0.5, (float) 0.5));
			}
			i++;
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		switch (itemPosition) {
		default:
		case 0: // Planning
			startActivity(new Intent(this, PlanningActivity.class));
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
			//startActivity(new Intent(this, GCPActivity.class));
			return false;
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_clear:
			clearWaypointsAndUpdate();
			return true;
		case R.id.menu_open_kmz:
			openGCPDialog();
			return true;
		case R.id.menu_zoom:
			zoomToExtents();
		default:
			return super.onMenuItemSelected(featureId, item);
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

	private boolean openGCPFile(String fileWithPath) {
		boolean returnValue = false;
		if (fileWithPath.endsWith(".kmz")) {
			returnValue = openKMZ(fileWithPath);
		} else if (fileWithPath.endsWith(".kml")) {
			returnValue = openKML(fileWithPath);
		}
		if (returnValue == true) {
			updateMarkers();
		}
		return returnValue;
	}

	private boolean openKML(String fileWithPath) {
		try {
			FileInputStream in = new FileInputStream(fileWithPath);
			KmlParser reader = new KmlParser();

			WPlist = reader.parse(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean openKMZ(String fileWithPath) {
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(
					fileWithPath));
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.getName().contains(".kml")) {
					KmlParser reader = new KmlParser();
					WPlist = reader.parse(zin);
				}
			}
			zin.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void openGCPDialog() {
		final String[] itemList = FileManager.loadKMZFileList();
		if (itemList.length == 0) {
			Toast.makeText(getApplicationContext(), R.string.no_waypoint_files,
					Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(itemList, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (openGCPFile(FileManager.getGCPPath()+ itemList[which])) {
					zoomToExtents();
					Toast.makeText(getApplicationContext(), itemList[which],
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.error_when_opening_file,
							Toast.LENGTH_SHORT).show();
				}
				updateMarkers();
			}

		});
		dialog.create().show();
	}


	@Override
	public boolean onMarkerClick(Marker marker) {
		int i = Integer.parseInt(marker.getTitle()) - 1;
		WPlist.get(i).set = !WPlist.get(i).set;
		updateMarkers();
		return true;
	}
}
