package org.droidplanner.android.graphic.map;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.mission.survey.SurveyData;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class CameraGroundOverlays {
	public ArrayList<Polygon> cameraOverlays = new ArrayList<com.google.android.gms.maps.model.Polygon>();
	private GoogleMap mMap;

	public CameraGroundOverlays(GoogleMap mMap) {
		this.mMap = mMap;
	}

	public void addOverlays(List<Coord2D> cameraLocations, SurveyData surveyData) {
		for (Coord2D latLng : cameraLocations) {
			addOneFootprint(latLng, surveyData);
		}
	}

	public void removeAll() {
		for (com.google.android.gms.maps.model.Polygon overlay : cameraOverlays) {
			overlay.remove();
		}
		cameraOverlays.clear();
	}

	private void addOneFootprint(Coord2D center, SurveyData surveyData) {
		double lng = surveyData.getLateralFootPrint().valueInMeters();
		double lateral = surveyData.getLongitudinalFootPrint().valueInMeters();
		double halfDiag = Math.hypot(lng, lateral) / 2;
		double angle = Math.toDegrees(Math.atan(lng / lateral));
		Double orientation = surveyData.getAngle();
		addRectangleOverlay(center, halfDiag, angle, orientation);

	}

	private void addRectangleOverlay(Coord2D center, double halfDiagonal,
			double centerAngle, Double orientation) {
		Coord2D c1 = GeoTools.newCoordFromBearingAndDistance(center,
				orientation - centerAngle, halfDiagonal);
		Coord2D c2 = GeoTools.newCoordFromBearingAndDistance(center,
				orientation + centerAngle, halfDiagonal);
		Coord2D c3 = GeoTools.newCoordFromBearingAndDistance(center,
				orientation + 180 - centerAngle, halfDiagonal);
		Coord2D c4 = GeoTools.newCoordFromBearingAndDistance(center,
				orientation + 180 + centerAngle, halfDiagonal);
		cameraOverlays.add(mMap.addPolygon(new PolygonOptions()
				.add(DroneHelper.CoordToLatLang(c1),
						DroneHelper.CoordToLatLang(c2),
						DroneHelper.CoordToLatLang(c3),
						DroneHelper.CoordToLatLang(c4))
				.fillColor(Color.argb(40, 0, 0, 127)).strokeWidth(1)
				.strokeColor(Color.argb(127, 0, 0, 255))));
	}

}