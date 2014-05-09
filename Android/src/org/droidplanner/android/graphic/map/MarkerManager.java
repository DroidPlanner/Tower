package org.droidplanner.android.graphic.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerManager {
	public GoogleMap mMap;
	public HashMap<Marker, MarkerSource> hashMap = new HashMap<Marker, MarkerSource>();

	public interface MarkerSource {
		MarkerOptions build(Context context);

		void update(Marker marker, Context context);
	}

	public MarkerManager(GoogleMap map) {
		this.mMap = map;
	}

	public void clean() {
		List<MarkerSource> emptyList = new ArrayList<MarkerSource>();
		removeOldMarkers(emptyList);
	}

	public <T extends MarkerSource> void updateMarkers(List<T> list, boolean draggable,
			Context context) {
		for (T object : list) {
			updateMarker(object, draggable, context);
		}
	}

	public void updateMarker(MarkerSource source, boolean draggable, Context context) {

		if (hashMap.containsValue(source)) {
			Marker marker = getMarkerFromSource(source);
			source.update(marker, context);
			marker.setDraggable(draggable);
		} else {
			addMarker(source, draggable, context);
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

	private void addMarker(MarkerSource object, boolean draggable,	Context context) {
		Marker marker = mMap.addMarker(object.build(context));
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