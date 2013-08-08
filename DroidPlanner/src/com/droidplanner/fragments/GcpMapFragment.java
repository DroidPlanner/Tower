package com.droidplanner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.fragments.helpers.OfflineMapFragment;
import com.droidplanner.fragments.markers.MarkerManager;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;

public class GcpMapFragment extends OfflineMapFragment implements
		OnMarkerClickListener {

	private OnGcpClickListner mListener;

	public MarkerManager markers;

	private GoogleMap mMap;

	public interface OnGcpClickListner {
		void onGcpClick(MarkerSource gcp);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		mMap.setOnMarkerClickListener(this);
		markers = new MarkerManager(mMap);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnGcpClickListner) activity;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		mListener.onGcpClick(markers.getSourceFromMarker(marker));
		return true;
	}

}
