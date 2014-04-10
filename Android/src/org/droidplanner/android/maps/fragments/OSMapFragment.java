package org.droidplanner.android.maps.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.R;
import org.droidplanner.android.maps.osm.RotationGestureOverlay;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

/**
 * This fragment abstracts the use and interaction with an OpenStreetMap view.
 *
 */
public class OSMapFragment extends Fragment {

    /**
     * osmdroid MapView handle.
     */
    private MapView mMapView;

    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_osmap, container, false);
        mMapView = (MapView) view.findViewById(R.id.osm_mapview);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setupMapUI();
    }

    private void setupMapUI(){
        final Context context = getActivity();

        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider
                (context), mMapView);
        mLocationOverlay = new MyLocationNewOverlay(context, new GpsMyLocationProvider(context),
                mMapView);
        final RotationGestureOverlay rotationOverlay = new RotationGestureOverlay(context,
                mMapView);
        rotationOverlay.setEnabled(true);

        mMapView.setUseSafeCanvas(true);
        mMapView.setMinZoomLevel(4);

        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);

        List<Overlay> mapOverlays = mMapView.getOverlays();
        mapOverlays.add(mLocationOverlay);
        mapOverlays.add(mCompassOverlay);
        mapOverlays.add(rotationOverlay);
    }

    @Override
    public void onPause(){
        super.onPause();
        mLocationOverlay.disableFollowLocation();
        mLocationOverlay.disableMyLocation();
        mCompassOverlay.disableCompass();
    }

    @Override
    public void onResume(){
        super.onResume();

        final ITileSource tileSource = TileSourceFactory.getTileSource(TileSourceFactory
                .DEFAULT_TILE_SOURCE.name());
        mMapView.setTileSource(tileSource);

        mLocationOverlay.enableFollowLocation();
        mLocationOverlay.enableMyLocation();
        mCompassOverlay.enableCompass();
    }

}
