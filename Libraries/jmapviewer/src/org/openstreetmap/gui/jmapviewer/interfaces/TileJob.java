// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import org.openstreetmap.gui.jmapviewer.Tile;

/**
 * Interface for implementing a tile loading job. Tiles are usually loaded via HTTP
 * or from a file.
 *
 * @author Dirk Stöcker
 */
public interface TileJob extends Runnable {

    /**
     * Function to return the tile associated with the job
     *
     * @return {@link Tile} to be handled
     */
    public Tile getTile();
}
