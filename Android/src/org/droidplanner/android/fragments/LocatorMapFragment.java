package org.droidplanner.android.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.core.helpers.coordinates.Coord2D;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("UseSparseArrays")
public class LocatorMapFragment extends DroneMap {

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

    public void zoomToFit() {
        // get visible mission coords
        final List<Coord2D> visibleCoords = new ArrayList<Coord2D>();

        // add GCS coord
        final Coord2D gcsPosition = mMapFragment.getGCSPosition();
        if(gcsPosition != null)
            visibleCoords.add(gcsPosition);

        mMapFragment.zoomToFit(visibleCoords);
    }

}
