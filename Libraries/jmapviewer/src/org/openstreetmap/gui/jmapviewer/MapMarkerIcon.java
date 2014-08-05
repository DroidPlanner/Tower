// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

/**
 * A simple implementation of the {@link MapMarker} interface. Each map marker
 * is draw with an image icon
 *
 */
public class MapMarkerIcon extends MapObjectImpl implements MapMarker {

    Coordinate coord;
	private Image img;

    public MapMarkerIcon(Coordinate coord) {
        super("");
        this.coord = coord;
        
        try {
			img = ImageIO.read(new File("../Android/res/drawable-hdpi/quad.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public Coordinate getCoordinate(){
        return coord;
    }
    public double getLat() {
        return coord.getLat();
    }

    public double getLon() {
        return coord.getLon();
    }

    public double getRadius() {
        return 0;
    }

    public STYLE getMarkerStyle() {
        return null;
    }

	public void paint(Graphics g, Point position, int radio) {
		g.drawImage(img, position.x - img.getWidth(null) / 2,
				position.y - img.getWidth(null) / 2, null);
		if (getLayer() == null || getLayer().isVisibleTexts())
			paintText(g, position);
	}

    public static Style getDefaultStyle(){
        return new Style(Color.ORANGE, new Color(200,200,200,200), null, getDefaultFont());
    }
    @Override
    public String toString() {
        return "MapMarker at " + getLat() + " " + getLon();
    }
    @Override
    public void setLat(double lat) {
        if(coord==null) coord = new Coordinate(lat,0);
        else coord.setLat(lat);
    }
    @Override
    public void setLon(double lon) {
        if(coord==null) coord = new Coordinate(0,lon);
        else coord.setLon(lon);
    }
}
