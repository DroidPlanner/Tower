// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

/**
 * Interface that allow cleaning the tile cache without specifying exact type of loader
 */
public interface CachedTileLoader {
    public void clearCache(TileSource source);
    public void clearCache(TileSource source, TileClearController controller);
}
