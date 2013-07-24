package com.droidplanner.fragments.markers;

import java.util.HashMap;
import java.util.List;

import com.droidplanner.fragments.GcpMapFragment;
import com.droidplanner.gcp.gcp;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerManager {
	public GoogleMap mMap;
	public HashMap<Marker, gcp> hashMap = new HashMap<Marker, gcp>();

	public MarkerManager(GoogleMap map) {
		this.mMap = map;
	}

	public void clear(GcpMapFragment gcpMapFragment) {
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

	public void updateMarker(gcp gcp) {
		if (hashMap.containsValue(gcp)) {
			GcpMarker.update(getMarkerFromGcp(gcp), gcp, 0);
		} else {
			addMarker(gcp);
		}
	}

	private void removeOldMarkers(List<gcp> gcpList) {
		for (Marker marker : hashMap.keySet()) {
			gcp gcp = getGcpFromMarker(marker);
			if (!gcpList.contains(gcp)) {
				removeMarker(gcp);
			}
		}
	}

	private boolean removeMarker(gcp gcp) {
		if (hashMap.containsValue(gcp)) {
			Marker marker = getMarkerFromGcp(gcp);
			hashMap.remove(marker);
			marker.remove();
			return true;
		} else {
			return false;
		}
	}

	private void addMarker(gcp gcp) {
		Marker marker = mMap.addMarker(GcpMarker.build(gcp, 0));
		hashMap.put(marker, gcp);
	}

	public Marker getMarkerFromGcp(gcp gcp) {
		for (Marker marker : hashMap.keySet()) {
			if (getGcpFromMarker(marker) == gcp) {
				return marker;
			}
		}
		return null;
	}

	public gcp getGcpFromMarker(Marker marker) {
		return hashMap.get(marker);
	}

}