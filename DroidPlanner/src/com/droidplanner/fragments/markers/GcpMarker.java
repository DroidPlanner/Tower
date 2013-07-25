package com.droidplanner.fragments.markers;

import com.droidplanner.R;
import com.droidplanner.gcp.Gcp;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GcpMarker{

	public static MarkerOptions build(Gcp gcp) {
		return new MarkerOptions()
		.position(gcp.coord)
		.title(String.valueOf(0))
		.icon(getIcon(gcp))
				.anchor((float) 0.5, (float) 0.5);
	}
	

	public static void update(Marker marker,Gcp gcp) {
		marker.setPosition(gcp.coord);	
		marker.setTitle(String.valueOf(0));
		marker.setIcon(getIcon(gcp));
	}

	private static BitmapDescriptor getIcon(Gcp gcp) {
		if (gcp.isMarked) {
			return BitmapDescriptorFactory
					.fromResource(R.drawable.placemark_circle_red);			
		} else {
			return BitmapDescriptorFactory
					.fromResource(R.drawable.placemark_circle_blue);
		}
	}
}