package org.droidplanner.android.graphic.map;

import android.content.Context;

import org.droidplanner.R;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GraphicDrone implements MarkerManager.MarkerSource {

	private Drone drone;

	public GraphicDrone(Drone drone) {
		this.drone = drone;
	}

    @Override
    public MarkerOptions build(Context context){
        return new MarkerOptions()
                .anchor((float) 0.5, (float) 0.5).position(new LatLng(0, 0))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.quad))
                .visible(false).flat(true);
    }

    @Override
    public void update(Marker marker, Context context){
        marker.setRotation((float)drone.orientation.getYaw());
        marker.setPosition(DroneHelper.CoordToLatLang(drone.GPS.getPosition()));
        marker.setVisible(true);
    }
}
