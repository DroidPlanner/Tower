package com.droidplanner.fragments.markers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerManager {
	public GoogleMap mMap;
	public HashMap<Marker, MarkerSource> hashMap = new HashMap<Marker, MarkerSource>();

	public interface MarkerSource {
		MarkerOptions build();

		void update(Marker marker);
	}

	public MarkerManager(GoogleMap map) {
		this.mMap = map;
	}

	public void cleanup() {
		List<MarkerSource> emptyList = new ArrayList<MarkerSource>();
		removeOldMarkers(emptyList);
	}

	public <T> void updateMarkers(List<T> list, boolean draggable) {
		for (T object : list) {
			updateMarker((MarkerSource) object, draggable);
		}
	}

	public void updateMarker(MarkerSource object, boolean draggable) {
		if (object != null) {
			if (hashMap.containsValue(object)) {
				Marker marker = getMarkerFromSource(object);
				((MarkerSource) object).update(marker);
				marker.setDraggable(draggable);
			} else {
				addMarker(object, draggable);
			}
		}
	}

	private <T> void removeOldMarkers(List<T> list) {
		List<MarkerSource> toRemove = new ArrayList<MarkerSource>();
		for (Marker marker : hashMap.keySet()) {
			MarkerSource object = getSourceFromMarker(marker);
			if (!list.contains(object)) {
				toRemove.add(object);
			}
		}
		for (MarkerSource markerSource : toRemove) {
			removeMarker(markerSource);
		}
	}

	private boolean removeMarker(MarkerSource object) {
		if (hashMap.containsValue(object)) {
			Marker marker = getMarkerFromSource(object);
			hashMap.remove(marker);
			marker.remove();
			return true;
		} else {
			return false;
		}
	}

	private void addMarker(MarkerSource object, boolean draggable) {
		Marker marker = mMap.addMarker(object.build());
		marker.setDraggable(draggable);
		hashMap.put(marker, object);
	}

	public Marker getMarkerFromSource(MarkerSource object) {
		for (Marker marker : hashMap.keySet()) {
			if (getSourceFromMarker(marker) == object) {
				return marker;
			}
		}
		return null;
	}

	public MarkerSource getSourceFromMarker(Marker marker) {
		return hashMap.get(marker);
	}

}