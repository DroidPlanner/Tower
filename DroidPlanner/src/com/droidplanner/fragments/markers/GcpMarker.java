package com.droidplanner.fragments.markers;

import com.droidplanner.R;
import com.droidplanner.gcp.gcp;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GcpMarker{

	public static MarkerOptions build(gcp gcp, int i) {
		return new MarkerOptions()
		.position(gcp.coord)
		.title(String.valueOf(i))
		.icon(getIcon(gcp))
				.anchor((float) 0.5, (float) 0.5);
	}
	
	private static BitmapDescriptor getIcon(gcp gcp) {
		if (gcp.isMarked) {
			return BitmapDescriptorFactory
					.fromResource(R.drawable.placemark_circle_red);			
		} else {
			return BitmapDescriptorFactory
					.fromResource(R.drawable.placemark_circle_blue);
		}
	}

	public static void update(Marker marker,gcp gcp, int i) {
		marker.setPosition(gcp.coord);	
		marker.setTitle(String.valueOf(i));
		marker.setIcon(getIcon(gcp));
	}

}