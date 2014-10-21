package org.droidplanner.android.graphic.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.droidplanner.R;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.core.helpers.coordinates.Coord2D;

public class GraphicLocator extends MarkerInfo.SimpleMarkerInfo {

    private Coord2D lastPosition;

    @Override
    public float getAnchorU() {
        return 0.5f;
    }

    @Override
    public float getAnchorV() {
        return 0.5f;
    }

    @Override
    public Coord2D getPosition(){
        return lastPosition;
    }

    @Override
    public Bitmap getIcon(Resources res){
        return BitmapFactory.decodeResource(res, R.drawable.quad);
    }

    @Override
    public boolean isVisible(){
        return true;
    }

    @Override
    public boolean isFlat(){
        return true;
    }

    @Override
    public float getRotation(){
        return 0;
    }

    public void setLastPosition(Coord2D lastPosition) {
        this.lastPosition = lastPosition;
    }
}