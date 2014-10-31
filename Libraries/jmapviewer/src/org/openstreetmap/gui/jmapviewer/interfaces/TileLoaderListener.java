// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import org.openstreetmap.gui.jmapviewer.Tile;

public interface TileLoaderListener {

    /**
     * Will be called if a new {@link Tile} has been loaded successfully.
     * Loaded can mean downloaded or loaded from file cache.
     *
     * @param tile
     */
    public void tileLoadingFinished(Tile tile, boolean success);

    /**
     * Return the {@link TileCache} class containing {@link Tile}
     * data for requested and loaded tiles
     *
     * @return tile information caching class
     */
    public TileCache getTileCache();
}
