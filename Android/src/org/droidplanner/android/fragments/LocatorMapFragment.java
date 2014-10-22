package org.droidplanner.android.fragments;

import android.location.LocationListener;
import android.widget.Toast;

import org.droidplanner.android.graphic.map.GraphicLocator;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;

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

        Toast.makeText(getActivity(), "Auto pan is not supported on this map.",
                Toast.LENGTH_LONG).show();
        return false;
    }

    public void updateLastPosition(Coord2D lastPosition) {
        graphicLocator.setLastPosition(lastPosition);
        mMapFragment.updateMarker(graphicLocator);
    }

    public void zoomToFit() {
        // add lastPosition
        final Coord2D lastPosition = graphicLocator.getPosition();
        if(lastPosition != null && !lastPosition.isEmpty()) {
            mMapFragment.zoomToFitMyLocation(Collections.singletonList(lastPosition));
        }
        else{
            mMapFragment.goToMyLocation();
        }
    }

    public void setLocationReceiver(LocationListener receiver){
        mMapFragment.setLocationListener(receiver);
    }
}