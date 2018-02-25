package co.aerobotics.android.graphic.map;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michaelwootton on 8/7/17.
 */

public class PolygonData {
    private String name = "";
    private List<LatLng> points = new ArrayList<>();
    private Boolean selected;
    private String id;

    public PolygonData(String name, List<LatLng> points, Boolean selected, String id){
        this.name = name;
        this.points = points;
        this.selected = selected;
        this.id = id;
    }

    public List<LatLng> getPoints(){
        return points;
    }

    public String getName(){
        return name;
    }

    public Boolean getSelected(){
        return selected;
    }

    public void setSelected(Boolean selected){
        this.selected = selected;
    }

    public int getColour(){
        if (selected){
            return Color.argb(150,255,255,255);
        } else{
            return Color.TRANSPARENT;
        }
    }

    public String getId() {
        return id;
    }
}
