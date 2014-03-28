package org.droidplanner.android.fragments.helpers;

import java.util.List;

import android.content.res.Resources;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapPath {
	private static final int DEFAULT_COLOR = Color.WHITE;
	private static final int DEFAULT_WITDH = 2;

	public interface PathSource {
		public List<LatLng> getPathPoints();
	}

	public Polyline missionPath;
	private GoogleMap mMap;
	private float width;
	private int color;

	public MapPath(GoogleMap mMap, Resources resources) {
		this(mMap, DEFAULT_COLOR, resources);
	}

	public MapPath(GoogleMap mMap, int color, Resources resources) {
		this(mMap, color, DEFAULT_WITDH, resources);
	}

	private MapPath(GoogleMap mMap, int color, float width, Resources resources) {
		this.mMap = mMap;
		this.color = color;
		this.width = scaleDpToPixels(width, resources);
	}

	private int scaleDpToPixels(double value, Resources res) {
		final float scale = res.getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}

	public void update(PathSource pathSource) {
		addToMapIfNeeded();
		List<LatLng> newPath = pathSource.getPathPoints();
		missionPath.setPoints(newPath);
	}

	private void addToMapIfNeeded() {
		if (missionPath == null) {
			PolylineOptions flightPath = new PolylineOptions();
			flightPath.color(color).width(width);
			missionPath = mMap.addPolyline(flightPath);
		}
	}
}