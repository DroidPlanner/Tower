package com.droidplanner.fragments;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.R;
import com.droidplanner.gcp.gcp;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GcpMapFragment extends OfflineMapFragment implements
		OnMarkerClickListener {

	private GoogleMap mMap;
	private OnGcpClickListner mListener;

	public interface OnGcpClickListner {
		void onGcpClick(int number);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
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
