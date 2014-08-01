// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

/**
 * {@link TileCache} implementation that stores all {@link Tile} objects in
 * memory up to a certain limit ({@link #getCacheSize()}). If the limit is
 * exceeded the least recently used {@link Tile} objects will be deleted.
 *
 * @author Jan Peter Stotz
 */
public class MemoryTileCache implements TileCache {

    protected static final Logger log = Logger.getLogger(MemoryTileCache.class.getName());

    /**
     * Default cache size
     */
    protected int cacheSize = 200;

    protected final Map<String, CacheEntry> hash;

    /**
     * List of all tiles in their last recently used order
     */
    protected final CacheLinkedListElement lruTiles;

    public MemoryTileCache() {
        hash = new HashMap<>(cacheSize);
        lruTiles = new CacheLinkedListElement();
    }

    @Override
    public synchronized void addTile(Tile tile) {
        CacheEntry entry = createCacheEntry(tile);
            hash.put(tile.getKey(), entry);
            lruTiles.addFirst(entry);
            if (hash.size() > cacheSize) {
                removeOldEntries();
            }
    }

    @Override
    public synchronized Tile getTile(TileSource source, int x, int y, int z) {
        CacheEntry entry = hash.get(Tile.getTileKey(source, x, y, z));
        if (entry == null)
            return null;
        // We don't care about placeholder tiles and hourglass image tiles, the
        // important tiles are the loaded ones
        if (entry.tile.isLoaded())
            lruTiles.moveElementToFirstPos(entry);
        return entry.tile;
    }

    /**
     * Removes the least recently used tiles
     */
    protected synchronized void removeOldEntries() {
        try {
            while (lruTiles.getElementCount() > cacheSize) {
                removeEntry(lruTiles.getLastElement());
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    protected synchronized void removeEntry(CacheEntry entry) {
        hash.remove(entry.tile.getKey());
        lruTiles.removeEntry(entry);
    }

    protected CacheEntry createCacheEntry(Tile tile) {
        return new CacheEntry(tile);
    }

    /**
     * Clears the cache deleting all tiles from memory
     */
    public synchronized void clear() {
        hash.clear();
        lruTiles.clear();
    }

    @Override
    public int getTileCount() {
        return hash.size();
    }

    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Changes the maximum number of {@link Tile} objects that this cache holds.
     *
     * @param cacheSize
     *            new maximum number of tiles
     */
    public synchronized void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        if (hash.size() > cacheSize)
            removeOldEntries();
    }

    /**
     * Linked list element holding the {@link Tile} and links to the
     * {@link #next} and {@link #prev} item in the list.
     */
    protected static class CacheEntry {
        Tile tile;

        CacheEntry next;
        CacheEntry prev;

        protected CacheEntry(Tile tile) {
            this.tile = tile;
        }

        public Tile getTile() {
            return tile;
        }

        public CacheEntry getNext() {
            return next;
        }

        public CacheEntry getPrev() {
            return prev;
        }

    }

    /**
     * Special implementation of a double linked list for {@link CacheEntry}
     * elements. It supports element removal in constant time - in difference to
     * the Java implementation which needs O(n).
     *
     * @author Jan Peter Stotz
     */
    protected static class CacheLinkedListElement {
        protected CacheEntry firstElement = null;
        protected CacheEntry lastElement;
        protected int elementCount;

        public CacheLinkedListElement() {
            clear();
        }

        public void clear() {
            elementCount = 0;
            firstElement = null;
            lastElement = null;
        }

        /**
         * Add the element to the head of the list.
         *
         * @param element new element to be added
         */
        public void addFirst(CacheEntry element) {
            if (element == null) return;
            if (elementCount == 0) {
                firstElement = element;
                lastElement = element;
                element.prev = null;
                element.next = null;
            } else {
                element.next = firstElement;
                firstElement.prev = element;
                element.prev = null;
                firstElement = element;
            }
            elementCount++;
        }

        /**
         * Removes the specified element from the list.
         *
         * @param element element to be removed
         */
        public void removeEntry(CacheEntry element) {
            if (element == null) return;
            if (element.next != null) {
                element.next.prev = element.prev;
            }
            if (element.prev != null) {
                element.prev.next = element.next;
            }
            if (element == firstElement)
                firstElement = element.next;
            if (element == lastElement)
                lastElement = element.prev;
            element.next = null;
            element.prev = null;
            elementCount--;
        }

        public void moveElementToFirstPos(CacheEntry entry) {
            if (firstElement == entry)
                return;
            removeEntry(entry);
            addFirst(entry);
        }

        public int getElementCount() {
            return elementCount;
        }

        public CacheEntry getLastElement() {
            return lastElement;
        }

        public CacheEntry getFirstElement() {
            return firstElement;
        }

    }
}
