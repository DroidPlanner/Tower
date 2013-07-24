package com.droidplanner.fragments;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.fragments.markers.GcpMarker;
import com.droidplanner.gcp.gcp;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.Marker;

public class GcpMapFragment extends OfflineMapFragment implements
		OnMarkerClickListener {

	private GoogleMap mMap;
	private OnGcpClickListner mListener;

	private HashMap<Marker, gcp> hashMap;

	public interface OnGcpClickListner {
		void onGcpClick(gcp gcp);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		mMap.setOnMarkerClickListener(this);
		hashMap =new HashMap<Marker, gcp>();
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnGcpClickListner) activity;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		mListener.onGcpClick(getGcpFromMarker(marker));
		return true;
	}
	
	public void clear(){
		for (Marker marker : hashMap.keySet()) {
				removeMarker(getGcpFromMarker(marker));
		}
	}

	public void updateMarkers(List<gcp> gcpList) {
		for (gcp gcp : gcpList) {
			updateMarker(gcp);
		}
		removeOldMarkers(gcpList);
	}

	private void removeOldMarkers(List<gcp> gcpList) {
		for (Marker marker : hashMap.keySet()) {
			gcp gcp = getGcpFromMarker(marker);
			if (!gcpList.contains(gcp)) {
				removeMarker(gcp);
			}
		}
	}

	public void updateMarker(gcp gcp) {
		if (hashMap.containsValue(gcp)) {
			GcpMarker.update(getMarkerFromGcp(gcp),gcp, 0);
		} else {
			addMarker(gcp);
		}
	}

	private void addMarker(gcp gcp) {
		Marker marker = mMap.addMarker(GcpMarker.build(gcp, 0));
		hashMap.put(marker, gcp);
	}

	public boolean removeMarker(gcp gcp) {
		if (hashMap.containsValue(gcp)) {
			Marker marker = getMarkerFromGcp(gcp);
			hashMap.remove(marker);
			marker.remove();
			return true;
		} else {
			return false;
		}
	}

	private gcp getGcpFromMarker(Marker marker) {
		return hashMap.get(marker);
	}

	private Marker getMarkerFromGcp(gcp gcp) {
		for (Marker marker : hashMap.keySet()) {
			if (getGcpFromMarker(marker) == gcp) {
				return marker;
			}
		}
		return null;
	}

}
