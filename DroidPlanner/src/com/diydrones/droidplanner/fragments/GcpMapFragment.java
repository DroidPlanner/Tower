package com.diydrones.droidplanner.fragments;

import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diydrones.droidplanner.R;
import com.diydrones.droidplanner.helpers.mapHelper;
import com.diydrones.droidplanner.waypoints.gcp;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GcpMapFragment extends MapFragment implements OnMarkerClickListener{

	private GoogleMap mMap;
	private OnGcpClickListner mListener;

	public interface OnGcpClickListner{
		void onGcpClick(int number);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		mMap = getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		mMap.setOnMarkerClickListener(this);

		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);

		mMap.setOnMarkerClickListener(this);
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnGcpClickListner) activity;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		int i = Integer.parseInt(marker.getTitle()) - 1;
		mListener.onGcpClick(i);
		return true;
	}

	public void updateMarkers(List<gcp> gcpList) {
		mMap.clear();
		int i = 1;
		for (gcp point : gcpList) {
			addGcpMarkerToMap(mMap, i, point.coord, point.isMarked);
			i++;
		}
	}

	public void zoomToExtents(List<gcp> gcpList) {
		if (!gcpList.isEmpty()) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (gcp point : gcpList) {
				builder.include(point.coord);
			}
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 30));
		}
	}

	/**
	 * Zoom to the extent of the waypoints should be used when the maps has not
	 * undergone the layout phase Assumes a map size of 480x360 px
	 * @param gcpList 
	 */
	public void zoomToExtentsFixed(List<gcp> gcpList) {
		if (!gcpList.isEmpty()) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (gcp point : gcpList) {
				builder.include(point.coord);
			}
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 480, 360, 30));
		}
	}



	public void setupMap(SharedPreferences prefs) {
		mapHelper.setupMapOverlay(
				mMap,
				prefs.getBoolean(
						"pref_advanced_use_offline_maps", false));
	}
	
	public void addGcpMarkerToMap(GoogleMap mMap, int i, LatLng coord,
			boolean isChecked) {
		if (isChecked) {
			mMap.addMarker(new MarkerOptions()
					.position(coord)
					.title(String.valueOf(i))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.placemark_circle_blue))
					.anchor((float) 0.5, (float) 0.5));
		} else {
			mMap.addMarker(new MarkerOptions()
					.position(coord)
					.title(String.valueOf(i))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.placemark_circle_red))
					.anchor((float) 0.5, (float) 0.5));
		}
	}
}
