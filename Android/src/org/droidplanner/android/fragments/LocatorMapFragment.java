package org.droidplanner.android.fragments;

import android.location.LocationListener;
import android.widget.Toast;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.android.graphic.map.GraphicLocator;
import org.droidplanner.android.utils.prefs.AutoPanMode;

import java.util.Collections;

public class LocatorMapFragment extends DroneMap {

    private final GraphicLocator graphicLocator = new GraphicLocator();

    @Override
    protected boolean isMissionDraggable() {
        return false;
    }

    @Override
    public boolean setAutoPanMode(AutoPanMode target) {
        if(target == AutoPanMode.DISABLED)
            return true;

        Toast.makeText(getActivity(), "Auto pan is not supported on this map.", Toast.LENGTH_LONG).show();
        return false;
    }

    public void updateLastPosition(LatLong lastPosition) {
        graphicLocator.setLastPosition(lastPosition);
        mMapFragment.updateMarker(graphicLocator);
    }

    public void zoomToFit() {
        // add lastPosition
        final LatLong lastPosition = graphicLocator.getPosition();
        if(lastPosition != null && lastPosition.getLongitude() != 0 && lastPosition.getLatitude() != 0) {
            mMapFragment.zoomToFit(Collections.singletonList(lastPosition));
        }
        else{
            mMapFragment.goToMyLocation();
        }
    }

    public void setLocationReceiver(LocationListener receiver){
        mMapFragment.setLocationListener(receiver);
    }
}