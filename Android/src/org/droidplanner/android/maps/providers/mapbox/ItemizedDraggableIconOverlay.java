package org.droidplanner.android.maps.providers.mapbox;

import android.content.Context;
import android.view.MotionEvent;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import org.droidplanner.android.maps.DPMap;

import java.util.List;

/**
 * Provides support for draggable markers.
 */
public class ItemizedDraggableIconOverlay extends ItemizedIconOverlay {

    public interface OnMarkerDragListener{
        abstract void onMarkerDrag(Marker marker);
        abstract void onMarkerDragEnd(Marker marker);
        abstract void onMarkerDragStart(Marker marker);
    }

    private Marker mDragMarker;
    private OnMarkerDragListener mMarkerDragListener;

    public ItemizedDraggableIconOverlay(Context pContext, List<Marker> pList, OnItemGestureListener<Marker> pOnItemGestureListener) {
        super(pContext, pList, pOnItemGestureListener);
    }

    public void setMarkerDragListener(OnMarkerDragListener markerDragListener){
        mMarkerDragListener = markerDragListener;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView){
        if(mDragMarker != null){
            if(event.getAction() == MotionEvent.ACTION_UP){
                if(mMarkerDragListener != null){
                    mMarkerDragListener.onMarkerDragEnd(mDragMarker);
                }
                mDragMarker = null;
            }
            else if(event.getAction() == MotionEvent.ACTION_MOVE){
                moveToEventPosition(event, mapView, mDragMarker);
                if(mMarkerDragListener != null){
                    mMarkerDragListener.onMarkerDrag(mDragMarker);
                }
            }
            else{
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView){
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(int index) {
                final Marker pressedMarker = getItem(index);
                mDragMarker = pressedMarker;
                if(mMarkerDragListener != null){
                    mMarkerDragListener.onMarkerDragStart(pressedMarker);
                }
                moveToEventPosition(event, mapView, pressedMarker);
                return true;
            }
        }));
    }

    public void moveToEventPosition(final MotionEvent event, final MapView mapView,
                                    final Marker marker){
        final Projection pj = mapView.getProjection();
        marker.setPoint((LatLng)pj.fromPixels(event.getX(), event.getY()));
        mapView.invalidate();
    }

    /**
     * When a content sensitive action is performed the content item needs to be identified. This
     * method does that and then performs the assigned task on that item.
     *
     * @return true if event is handled false otherwise
     */
    protected boolean activateSelectedItems(final MotionEvent event, final MapView mapView,
                                          final ActiveItem task) {
        final Projection projection = mapView.getProjection();
        final float x = event.getX();
        final float y = event.getY();
        for (int i = 0; i < this.mItemList.size(); ++i) {
            final Marker item = getItem(i);
            if (markerHitTest(item, projection, x, y)) {
                if (task.run(i)) {
                    this.setFocus(item);
                    return true;
                }
            }
        }
        return false;
    }
}
