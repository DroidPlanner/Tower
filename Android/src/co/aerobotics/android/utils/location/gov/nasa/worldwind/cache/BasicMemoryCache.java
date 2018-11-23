/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dcollins
 * @version $Id$
 */
public class BasicMemoryCache implements MemoryCache
{
    protected static class CacheEntry implements Comparable<CacheEntry>
    {
        protected Object key;
        protected Object value;
        protected long size;
        protected long lastUsed;

        protected CacheEntry(Object key, Object value, long size)
        {
            this.key = key;
            this.value = value;
            this.size = size;
            this.lastUsed = System.nanoTime();
        }

        public int compareTo(CacheEntry that)
        {
            return this.lastUsed < that.lastUsed ? -1 : this.lastUsed == that.lastUsed ? 0 : 1;
        }
    }

    protected String name;
    protected ConcurrentHashMap<Object, CacheEntry> entries;
    protected CopyOnWriteArrayList<CacheListener> listeners;
    protected AtomicLong capacity = new AtomicLong();
    protected AtomicLong usedCapacity = new AtomicLong();
    protected AtomicLong lowWater = new AtomicLong();
    protected final Object lock = new Object();

    /**
     * Constructs a new cache using <code>capacity</code> for maximum size, and <code>loWater</code> for the low water.
     *
     * @param loWater  the low water level.
     * @param capacity the maximum capacity.
     */
    public BasicMemoryCache(long loWater, long capacity)
    {
        this.entries = new ConcurrentHashMap<Object, CacheEntry>();
        this.listeners = new CopyOnWriteArrayList<CacheListener>();
        this.capacity.set(capacity);
        this.usedCapacity.set(0L);
        this.lowWater.set(loWater);
    }

    /** {@inheritDoc} */
    public String getName()
    {
        return this.name;
    }

    /** {@inheritDoc} */
    public void setName(String name)
    {
        this.name = name;
    }

    /** {@inheritDoc} */
    public Object get(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        CacheEntry entry; // don't need to lock because call is atomic
        synchronized (this.lock)
        {
            entry = this.entries.get(key);

            if (entry != null)
            {
                // System.nanoTime overflows once every 292 years. Overflow results in a slowing of the cache until WW
                // is restarted or the cache is cleared.
                entry.lastUsed = System.nanoTime();
            }
        }

        return entry != null ? entry.value : null;
    }

    /** {@inheritDoc} */
    public void put(Object key, Object value, long size)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (value == null)
        {
            String msg = Logging.getMessage("nullValue.ValueIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (size < 1)
        {
            String msg = Logging.getMessage("MemoryCache.SizeIsLessThanOne", size);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        long cap = this.capacity.get();

        if (size > cap)
        {
            String msg = Logging.getMessage("MemoryCache.SizeIsLargerThanCapacity", size, cap);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        CacheEntry entry = new CacheEntry(key, value, size);

        synchronized (this.lock)
        {
            CacheEntry existing = this.entries.get(key);
            if (existing != null) // replacing
                this.removeEntry(existing);

            if (this.usedCapacity.get() + size > cap)
                this.makeSpace(size);

            this.usedCapacity.addAndGet(size);
            this.entries.put(entry.key, entry);
        }
    }

    /** {@inheritDoc} */
    public void put(Object key, Cacheable value)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (value == null)
        {
            String msg = Logging.getMessage("nullValue.ValueIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.put(key, value, value.getSizeInBytes());
    }

    /** {@inheritDoc} */
    public boolean contains(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        synchronized (this.lock)
        {
            return this.entries.containsKey(key);
        }
    }

    /** {@inheritDoc} */
    public void remove(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        synchronized (this.lock)
        {
            CacheEntry entry = this.entries.get(key);
            if (entry != null)
                this.removeEntry(entry);
        }
    }

    /** {@inheritDoc} */
    public void clear()
    {
        synchronized (this.lock)
        {
            for (CacheEntry entry : this.entries.values())
            {
                this.removeEntry(entry);
            }
        }
    }

    /** {@inheritDoc} */
    public int getNumObjects()
    {
        return this.entries.size();
    }

    /** {@inheritDoc} */
    public long getCapacity()
    {
        return this.capacity.get();
    }

    /** {@inheritDoc} */
    public long getUsedCapacity()
    {
        return this.usedCapacity.get();
    }

    /** {@inheritDoc} */
    public long getFreeCapacity()
    {
        return Math.max(this.capacity.get() - this.usedCapacity.get(), 0);
    }

    /** {@inheritDoc} */
    public void setCapacity(long newCapacity)
    {
        this.capacity.set(newCapacity);
    }

    /** {@inheritDoc} */
    public long getLowWater()
    {
        return this.lowWater.get();
    }

    /** {@inheritDoc} */
    public void setLowWater(long loWater)
    {
        if (loWater < this.capacity.get() && loWater >= 0)
        {
            this.lowWater.set(loWater);
        }
    }

    /** {@inheritDoc} */
    public void addCacheListener(CacheListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.warning(msg);
            throw new IllegalArgumentException(msg);
        }

        this.listeners.add(listener);
    }

    /** {@inheritDoc} */
    public void removeCacheListener(CacheListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.warning(msg);
            throw new IllegalArgumentException(msg);
        }

        this.listeners.remove(listener);
    }

    /**
     * Removes <code>entry</code> from the cache. To remove an entry using its key, use <code>remove()</code>.
     *
     * @param entry The entry (as opposed to key) of the item to be removed.
     */
    protected void removeEntry(CacheEntry entry) // MUST BE CALLED WITHIN SYNCHRONIZED
    {
        // all removal passes through this function,
        // so the reduction in "currentUsedCapacity" and listener notification is done here

        if (this.entries.remove(entry.key) != null) // returns null if entry does not exist
        {
            this.usedCapacity.addAndGet(-entry.size);

            for (MemoryCache.CacheListener listener : this.listeners)
            {
                try
                {
                    listener.entryRemoved(entry.key, entry.value);
                }
                catch (Exception e)
                {
                    listener.removalException(e, entry.key, entry.value);
                }
            }
        }
    }

    /**
     * Makes at least <code>spaceRequired</code> space in the cache. If spaceRequired is less than (capacity-lowWater),
     * makes more space. Does nothing if capacity is less than spaceRequired.
     *
     * @param spaceRequired the amount of space required.
     */
    protected void makeSpace(long spaceRequired) // MUST BE CALLED WITHIN SYNCHRONIZED
    {
        if (spaceRequired > this.capacity.get() || spaceRequired < 0)
            return;

        CacheEntry[] timeOrderedEntries = new CacheEntry[this.entries.size()];
        Arrays.sort(this.entries.values().toArray(timeOrderedEntries)); // TODO

        int i = 0;
        while (this.getFreeCapacity() < spaceRequired || this.getUsedCapacity() > this.getLowWater())
        {
            if (i < timeOrderedEntries.length)
            {
                this.removeEntry(timeOrderedEntries[i++]);
            }
        }
    }
}
