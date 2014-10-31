// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.List;

/**
 * Interface to be implemented by polygons that can be displayed on the map.
 *
 * @author Vincent
 */
public interface MapPolygon extends MapObject{

    /**
     * @return Latitude/Longitude of each point of polygon
     */
    public List<? extends ICoordinate> getPoints();

    /**
     * Paints the map rectangle on the map. The <code>points</code>
     * are specifying the coordinates within <code>g</code>
     *
     * @param g
     * @param points
     */
    public void paint(Graphics g, List<Point> points);

    /**
     * Paints the map rectangle on the map. The <code>polygon</code>
     * is specifying the coordinates within <code>g</code>
     *
     * @param g
     * @param polygon
     */
    public void paint(Graphics g, Polygon polygon);
}
