package org.droidplanner.android.maps.providers.mapbox;

import android.location.Location;
import android.location.LocationListener;

import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Provide facilities for a listener to be notified of location updates.
 */
public class UserLocationProvider extends UserLocationOverlay {
    private LocationListener mLocationListener;

    public UserLocationProvider(GpsLocationProvider myLocationProvider, MapView mapView, int arrowId, int personId) {
        super(myLocationProvider, mapView, arrowId, personId);
    }

    public UserLocationProvider(GpsLocationProvider myLocationProvider, MapView mapView) {
        super(myLocationProvider, mapView);
    }

    public void setLocationListener(LocationListener listener){
        mLocationListener = listener;
    }

    @Override
    public void onLocationChanged(Location location, GpsLocationProvider source){
        super.onLocationChanged(location, source);

        if(mLocationListener != null){
            mLocationListener.onLocationChanged(location);
        }
    }
}
