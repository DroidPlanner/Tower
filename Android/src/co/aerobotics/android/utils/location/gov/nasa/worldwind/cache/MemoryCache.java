/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

/**
 * @author dcollins
 * @version $Id$
 */
public interface MemoryCache
{
    /**
     * Provides the interface for cache clients to be notified of key events. Currently the only key event is the
     * removal of an entry from the cache. A client may need to know a removal instigated by the cache occurred in order
     * to adjust its own state or to free resources associated with the removed entry.
     */
    public interface CacheListener
    {
        /**
         * Called just after an entry has been removed from the cache. Listeners should deallocate any resources that
         * won't be deallocated by normal garbage collection.
         *
         * @param key   the entry's cache key.
         * @param value the cached object.
         */
        public void entryRemoved(Object key, Object value);

        /**
         * Called when an exception occurs within the {@link #entryRemoved(Object, Object)} call.
         *
         * @param exception the exception that occurred.
         * @param key       the entry's cache key.
         * @param value     the cached object.
         */
        public void removalException(Throwable exception, Object key, Object value);
    }

    String getName();

    void setName(String name);

    /**
     * Retrieves the requested item from the cache. If <code>key</code> is null or the item is not found, this method
     * returns null.
     *
     * @param key an <code>Object</code> used to represent the item to retrieve.
     *
     * @return the requested <code>Object</code> if found, null otherwise.
     */
    Object get(Object key);

    /**
     * Attempts to add the object <code>clientObject</code>, with size <code>objectSize</code> and referred to by
     * <code>key</code> to the cache. <code>objectSize</code> is the size in cache units, but is not checked for
     * accuracy. Returns whether or not the add was successful.
     * <p/>
     * Note that the size passed in may be used, rather than the real size of the object. In some implementations, the
     * accuracy of the space used calls will depend on the collection of these sizes, rather than actual size.
     * <p/>
     * This method should be declared <code>synchronized</code> when it is implemented.
     *
     * @param key   an object used to reference the cached item.
     * @param value the item to be cached.
     * @param size  the size of the item in cache units.
     */
    void put(Object key, Object value, long size);

    /**
     * Attempts to add the <code>Cacheable</code> object referenced by the key. No explicit size value is required as
     * this method queries the Cacheable to discover the size.
     * <p/>
     * This method should be declared <code>synchronized</code> when it is implemented.
     *
     * @param key   an object used to reference the cached item.
     * @param value the item to be cached.
     *
     * @see Cacheable
     */
    void put(Object key, Cacheable value);

    /**
     * Returns true if the cache contains the item referenced by key. No guarantee is made as to whether or not the item
     * will remain in the cache for any period of time.
     * <p/>
     * This function does not cause the object referenced by the key to be marked as accessed. <code>get</code> should
     * be used for that purpose.
     *
     * @param key The key of a specific object.
     *
     * @return true if the cache holds the item referenced by key.
     *
     * @throws IllegalArgumentException if <code>key</code> is null.
     */
    boolean contains(Object key);

    /**
     * Remove the object reference by key from the cache. If no object with the corresponding key is found, this method
     * returns immediately.
     *
     * @param key the key of the object to be removed.
     *
     * @throws IllegalArgumentException if <code>key</code> is null.
     */
    void remove(Object key);

    /**
     * Empties the cache. After calling <code>clear()</code> on a <code>MemoryCache</code>, calls relating to used
     * capacity and number of items should return zero and the free capacity should be the maximum capacity.
     * <p/>
     * This method should be synchronized when it is implemented and should notify all <code>CacheListener</code>s of
     * entries removed.
     */
    void clear();

    /**
     * Retrieve the number of items stored in the <code>MemoryCache</code>.
     *
     * @return the number of items in the cache.
     */
    int getNumObjects();

    /**
     * Retrieves the maximum size of the cache.
     *
     * @return the maximum size of the <code>MemoryCache</code>.
     */
    long getCapacity();

    /**
     * Retrieves the amount of used <code>MemoryCache</code> space. The value returned is in cache units.
     *
     * @return the long value of the number of cache units used by cached items.
     */
    long getUsedCapacity();

    /**
     * Retrieves the available space for storing new items.
     *
     * @return the long value of the remaining space for storing cached items.
     */
    long getFreeCapacity();

    /**
     * Sets the new capacity for the cache. When decreasing cache size, it is recommended to check that the lowWater
     * variable is suitable. If the capacity infringes on items stored in the cache, these items are removed. Setting a
     * new low water is up to the user, that is, it remains unchanged and may be higher than the maximum capacity. When
     * the low water level is higher than or equal to the maximum capacity, it is ignored, which can lead to poor
     * performance when adding entries.
     *
     * @param capacity the new capacity of the cache.
     */
    void setCapacity(long capacity);

    /**
     * Retrieves the low water value of the <code>MemoryCache</code>. When a <code>MemoryCache</code> runs out of free
     * space, it must remove some items if it wishes to add any more. It continues removing items until the low water
     * level is reached. Not every <code>MemoryCache</code> necessarily uses the low water system, so this may not
     * return a useful value.
     *
     * @return the low water value of the <code>MemoryCache</code>.
     */
    long getLowWater();

    /**
     * Sets the new low water capacity value for this <code>MemoryCache</code>. When a <code>MemoryCache</code> runs out
     * of free space, it must remove some items if it wishes to add any more. It continues removing items until the low
     * water level is reached. Not every <code>MemoryCache</code> necessarily uses the low water system, so this method
     * may not have any actual effect in some implementations.
     *
     * @param loWater the new low water value.
     */
    void setLowWater(long loWater);

    /**
     * Adds a new <code>cacheListener</code>, which will be sent notification whenever an entry is removed from the
     * cache.
     *
     * @param listener the new <code>MemoryCache.CacheListener</code>
     */
    void addCacheListener(CacheListener listener);

    /**
     * Removes a <code>CacheListener</code>, notifications of events will no longer be sent to this listener.
     *
     * @param listener the cache listener to remove.
     */
    void removeCacheListener(CacheListener listener);
}
