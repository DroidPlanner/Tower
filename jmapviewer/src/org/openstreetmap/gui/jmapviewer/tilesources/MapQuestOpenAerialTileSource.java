// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.Coordinate;

public class MapQuestOpenAerialTileSource extends AbstractMapQuestTileSource {

    private static final String PATTERN = "http://oatile%d.mqcdn.com/tiles/1.0.0/sat";

    public MapQuestOpenAerialTileSource() {
        super("MapQuest Open Aerial", PATTERN);
    }

    @Override
    public String getAttributionText(int zoom, Coordinate topLeft, Coordinate botRight) {
        return "Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency - "+MAPQUEST_ATTRIBUTION;
    }

    @Override
    public String getAttributionLinkURL() {
        return MAPQUEST_WEBSITE;
    }
}
