package com.droidplanner.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.droidplanner.drone.variables.mission.survey.SurveyData;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class CameraGroundOverlays {
	public ArrayList<Polygon> cameraOverlays = new ArrayList<com.google.android.gms.maps.model.Polygon>();
	private GoogleMap mMap;

	public CameraGroundOverlays(GoogleMap mMap) {
		this.mMap = mMap;
	}

	public void addOverlays(List<LatLng> cameraLocations,
			SurveyData surveyData) {
		for (LatLng latLng : cameraLocations) {
			addOneFootprint(latLng, surveyData);
		}
	}


	public void removeAll() {
		for (com.google.android.gms.maps.model.Polygon overlay : cameraOverlays) {
			overlay.remove();
		}
		cameraOverlays.clear();
	}

	private void addOneFootprint(LatLng latLng, SurveyData surveyData) {
		double lng = surveyData.getLateralFootPrint().valueInMeters();
		double lateral = surveyData.getLongitudinalFootPrint().valueInMeters();
		double halfDiag = Math.hypot(lng, lateral) / 2;
		double angle = Math.toDegrees(Math.atan(lng / lateral));
		Double orientation = surveyData.getAngle();
		addRectangleOverlay(latLng, halfDiag, angle, orientation);
		
	}

	private void addRectangleOverlay(LatLng center, double halfDiagonal,
			double centerAngle, Double orientation) {
		cameraOverlays.add(mMap.addPolygon(new PolygonOptions()
		.add(GeoTools.newCoordFromBearingAndDistance(center,
				orientation - centerAngle, halfDiagonal),
				GeoTools.newCoordFromBearingAndDistance(center,
						orientation + centerAngle, halfDiagonal),
						GeoTools.newCoordFromBearingAndDistance(center,
								orientation + 180 - centerAngle, halfDiagonal),
								GeoTools.newCoordFromBearingAndDistance(center,
										orientation + 180 + centerAngle, halfDiagonal))
										.fillColor(Color.argb(40, 0, 0, 127)).strokeWidth(1)
										.strokeColor(Color.argb(127, 0, 0, 255))));
	}

}