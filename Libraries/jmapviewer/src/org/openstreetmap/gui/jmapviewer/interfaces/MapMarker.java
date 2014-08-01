// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 * Interface to be implemented by all one dimensional elements that can be displayed on the map.
 *
 * @author Jan Peter Stotz
 * @see JMapViewer#addMapMarker(MapMarker)
 * @see JMapViewer#getMapMarkerList()
 */
public interface MapMarker extends MapObject, ICoordinate{

    public static enum STYLE {FIXED, VARIABLE}

    /**
     * @return Latitude and Longitude of the map marker position
     */
    public Coordinate getCoordinate();
    /**
     * @return Latitude of the map marker position
     */
    public double getLat();

    /**
     * @return Longitude of the map marker position
     */
    public double getLon();

    /**
     * @return Radius of the map marker position
     */
    public double getRadius();

    /**
     * @return Style of the map marker
     */
    public STYLE getMarkerStyle();

    /**
     * Paints the map marker on the map. The <code>position</code> specifies the
     * coordinates within <code>g</code>
     *
     * @param g
     * @param position
     * @param radio
     */
    public void paint(Graphics g, Point position, int radio);
}
