package org.droidplanner.glass.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

/**
 * This fragment renders a map view on glass.
 * @author Fredia Huya-Kouadio
 */
public class GlassMapFragment extends Fragment {

    private static final String TAG = GlassMapFragment.class.getSimpleName();

    private static final int MENU_LAST_ID = Menu.FIRST + 1; // Always set to last unused id

    public static final String PREFS_TILE_SOURCE = "tilesource";
    public static final String PREFS_SCROLL_X = "scrollX";
    public static final String PREFS_SCROLL_Y = "scrollY";
    public static final String PREFS_ZOOM_LEVEL = "zoomLevel";
    public static final String PREFS_SHOW_LOCATION = "showLocation";
    public static final String PREFS_SHOW_COMPASS = "showCompass";

    private MapView mMapView;
    private CompassOverlay mCompassOverlay;
    private MyLocationNewOverlay mLocationOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = inflater.getContext().getApplicationContext();
        final ResourceProxy resProxy = new ResourceProxyImpl(context);
        mMapView = new MapView(context, 256, resProxy);
        mMapView.setUseSafeCanvas(true);
        setHardwareAccelerationOff();
        return mMapView;
    }

    private void setHardwareAccelerationOff(){
        //Turn off hardware acceleration here, or in manifest
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        final Context context = getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Only do static initialization if needed
        if(CloudmadeUtil.getCloudmadeKey().length() == 0){
            CloudmadeUtil.retrieveCloudmadeKey(context);
        }

        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider
                (context), mMapView);
        mLocationOverlay = new MyLocationNewOverlay(context, new GpsMyLocationProvider(context),
                mMapView);

        final MinimapOverlay minimapOverlay = new MinimapOverlay(context,
                mMapView.getTileRequestCompleteHandler());
        minimapOverlay.setWidth(dm.widthPixels / 5);
        minimapOverlay.setHeight(dm.heightPixels / 5);

        final ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(context);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        List<Overlay> mapOverlays = mMapView.getOverlays();
        mapOverlays.add(mLocationOverlay);
        mapOverlays.add(mCompassOverlay);
        mapOverlays.add(minimapOverlay);
        mapOverlays.add(scaleBarOverlay);

        mMapView.getController().setZoom(prefs.getInt(PREFS_ZOOM_LEVEL, 1));
        mMapView.scrollTo(prefs.getInt(PREFS_SCROLL_X, 0), prefs.getInt(PREFS_SCROLL_Y, 0));

        mLocationOverlay.enableMyLocation();
        mCompassOverlay.enableCompass();

        setHasOptionsMenu(true);
    }

    @Override
    public void onPause(){
        final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences
                (getActivity()).edit();

        edit.putString(PREFS_TILE_SOURCE, mMapView.getTileProvider().getTileSource().name());
        edit.putInt(PREFS_SCROLL_X, mMapView.getScrollX());
        edit.putInt(PREFS_SCROLL_Y, mMapView.getScrollY());
        edit.putInt(PREFS_ZOOM_LEVEL, mMapView.getZoomLevel());
        edit.putBoolean(PREFS_SHOW_LOCATION, mLocationOverlay.isMyLocationEnabled());
        edit.putBoolean(PREFS_SHOW_COMPASS, mCompassOverlay.isCompassEnabled());
        edit.commit();

        mLocationOverlay.disableMyLocation();
        mCompassOverlay.disableCompass();

        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        final String tileSourceName = prefs.getString(PREFS_TILE_SOURCE,
                TileSourceFactory.DEFAULT_TILE_SOURCE.name());

        try{
            final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
            mMapView.setTileSource(tileSource);
        }catch(IllegalArgumentException e){
            Log.e(TAG, e.getMessage(), e);
        }

        if(prefs.getBoolean(PREFS_SHOW_LOCATION, false)){
            mLocationOverlay.enableMyLocation();
        }

        if(prefs.getBoolean(PREFS_SHOW_COMPASS, false)){
            mCompassOverlay.enableCompass();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        //Put overlay items first
        mMapView.getOverlayManager().onCreateOptionsMenu(menu, MENU_LAST_ID, mMapView);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        mMapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID, mMapView);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(mMapView.getOverlayManager().onOptionsItemSelected(item, MENU_LAST_ID, mMapView))
            return true;
        return super.onOptionsItemSelected(item);
    }
}

