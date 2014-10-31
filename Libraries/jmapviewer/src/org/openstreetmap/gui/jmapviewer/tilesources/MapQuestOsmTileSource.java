// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import org.openstreetmap.gui.jmapviewer.Coordinate;

public class MapQuestOsmTileSource extends AbstractMapQuestTileSource {

    private static final String PATTERN = "http://otile%d.mqcdn.com/tiles/1.0.0/osm";

    public MapQuestOsmTileSource() {
        super("MapQuest-OSM", PATTERN);
    }
    
    @Override
    public String getAttributionText(int zoom, Coordinate topLeft,
            Coordinate botRight) {
        return super.getAttributionText(zoom, topLeft, botRight)+" - "+MAPQUEST_ATTRIBUTION;
    }
}
