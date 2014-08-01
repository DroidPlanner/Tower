// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.Style;

public interface MapObject {

    public Layer getLayer();
    public void setLayer(Layer layer);
    public Style getStyle();
    public Style getStyleAssigned();
    public Color getColor();
    public Color getBackColor();
    public Stroke getStroke();
    public Font getFont();
    public String getName();
    public boolean isVisible();
}
