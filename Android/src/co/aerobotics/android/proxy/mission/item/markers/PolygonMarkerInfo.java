package co.aerobotics.android.proxy.mission.item.markers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import co.aerobotics.android.R;
import co.aerobotics.android.maps.MarkerInfo;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;

/**
 */
public class PolygonMarkerInfo extends MarkerInfo {

	private LatLong mPoint;
	private final MissionItemProxy markerOrigin;
    private final Survey survey;
    private final int polygonIndex;

	public PolygonMarkerInfo(LatLong point, MissionItemProxy origin, Survey mSurvey, int index) {
		this.markerOrigin = origin;
		mPoint = point;
		survey = mSurvey;
		polygonIndex = index;
	}

	public Survey getSurvey(){
		return survey;
	}

	public int getIndex(){
		return polygonIndex;
	}



	
	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		Bitmap imageBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_wp_map_selected);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 80, 80, false);
		return resizedBitmap;
	}

	@Override
	public com.o3dr.services.android.lib.coordinate.LatLong getPosition() {
		return mPoint;
	}

	@Override
	public void setPosition(LatLong coord) {
		mPoint = coord;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}


	public MissionItemProxy getMarkerOrigin() {
		return markerOrigin;
	}
}
