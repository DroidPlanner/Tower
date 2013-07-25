package com.droidplanner.fragments.markers;

import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerManager<T> {
	public GoogleMap mMap;
	public HashMap<Marker, T> hashMap = new HashMap<Marker, T>();

	public interface markerSource{
		MarkerOptions build();

		void update(Marker markerFromGcp);
	}
	
	public MarkerManager(GoogleMap map) {
		this.mMap = map;
	}

	public void clear() {
		for (Marker marker : hashMap.keySet()) {
			removeMarker(getObjectFromMarker(marker));
		}
	}

	public void updateMarkers(List<T> list) {
		for (T object : list) {
			updateMarker(object);
		}
		removeOldMarkers(list);
	}

	public void updateMarker(T marker) {
		if (hashMap.containsValue(marker)) {
			((markerSource) marker).update(getMarkerFromObject(marker));
		} else {
			addMarker(marker);
		}
	}

	private void removeOldMarkers(List<T> list) {
		for (Marker marker : hashMap.keySet()) {
			T object = getObjectFromMarker(marker);
			if (!list.contains(object)) {
				removeMarker(object);
			}
		}
	}

	private boolean removeMarker(T object) {
		if (hashMap.containsValue(object)) {
			Marker marker = getMarkerFromObject(object);
			hashMap.remove(marker);
			marker.remove();
			return true;
		} else {
			return false;
		}
	}

	private void addMarker(T object) {
		Marker marker = mMap.addMarker(((markerSource) object).build());
		hashMap.put(marker, object);
	}

	public Marker getMarkerFromObject(T object) {
		for (Marker marker : hashMap.keySet()) {
			if (getObjectFromMarker(marker) == object) {
				return marker;
			}
		}
		return null;
	}

	public T getObjectFromMarker(Marker marker) {
		return hashMap.get(marker);
	}

}