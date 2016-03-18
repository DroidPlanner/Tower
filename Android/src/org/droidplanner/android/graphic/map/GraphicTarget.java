package org.droidplanner.android.graphic.map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.MarkerInfo;

public class GraphicTarget extends MarkerInfo.SimpleMarkerInfo {

	private final Context context;
    private LatLong mPosition;

	public GraphicTarget(Context context) {
		this.context = context;
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	public boolean isValid() {
		return (mPosition != null);
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return BitmapFactory.decodeResource(res, R.drawable.ic_wp_target);
	}

	@Override
	public LatLong getPosition() {
        return mPosition;
	}

	@Override
	public void setPosition(LatLong position){
        // Move the location
        mPosition = position;
	}

	@Override
	public String getSnippet() {
		return "Target location";
	}

	@Override
	public String getTitle() {
		return "Target";
	}

	@Override
	public boolean isVisible() {
		return isValid();
	}

	@Override
	public boolean isDraggable() {
		return true;
	}
}
