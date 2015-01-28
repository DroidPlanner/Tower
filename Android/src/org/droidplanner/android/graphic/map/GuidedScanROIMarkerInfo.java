package org.droidplanner.android.graphic.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.MarkerInfo;

/**
 * Created by Fredia Huya-Kouadio on 1/27/15.
 */
public class GuidedScanROIMarkerInfo extends MarkerInfo.SimpleMarkerInfo {

    private LatLong roiCoord;

    @Override
    public void setPosition(LatLong coord){
        this.roiCoord = coord;
    }

    @Override
    public LatLong getPosition(){
        return roiCoord;
    }

    @Override
    public Bitmap getIcon(Resources res){
        return BitmapFactory.decodeResource(res, R.drawable.ic_roi);
    }

    @Override
    public boolean isVisible(){
        return roiCoord != null;
    }

    @Override
    public float getAnchorU() {
        return 0.5f;
    }

    @Override
    public float getAnchorV() {
        return 0.5f;
    }
}
