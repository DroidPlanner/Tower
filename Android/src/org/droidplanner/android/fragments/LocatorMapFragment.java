package org.droidplanner.android.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicLocator;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("UseSparseArrays")
public class LocatorMapFragment extends DroneMap {

    private GraphicLocator graphicLocator = new GraphicLocator();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        View view = super.onCreateView(inflater, viewGroup, bundle);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

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

    public Coord2D getGCSPosition() {
        return mMapFragment.getGCSPosition();
    }

    public void updateLastPosition(Coord2D lastPosition) {
        graphicLocator.setLastPosition(lastPosition);
        mMapFragment.updateMarker(graphicLocator);
    }

    public void zoomToFit() {
        final List<Coord2D> visibleCoords = new ArrayList<Coord2D>();

        // add lastPosition
        final Coord2D lastPosition = graphicLocator.getPosition();
        if(lastPosition != null && !lastPosition.isEmpty())
            visibleCoords.add(lastPosition);

        // add GCS coord
        final Coord2D gcsPosition = mMapFragment.getGCSPosition();
        if(gcsPosition != null && !gcsPosition.isEmpty())
            visibleCoords.add(gcsPosition);

        if(!visibleCoords.isEmpty())
            mMapFragment.zoomToFit(visibleCoords);
    }
}
