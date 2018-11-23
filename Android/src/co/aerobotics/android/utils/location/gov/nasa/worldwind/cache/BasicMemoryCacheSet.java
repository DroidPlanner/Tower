/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dcollins
 * @version $Id$
 */
public class BasicMemoryCacheSet implements MemoryCacheSet
{
    protected ConcurrentHashMap<String, MemoryCache> caches = new ConcurrentHashMap<String, MemoryCache>();
    protected ArrayList<PerformanceStatistic> performanceStatistics;

    public BasicMemoryCacheSet()
    {
    }

    /** {@inheritDoc} */
    public synchronized MemoryCache get(String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.caches.get(key);
    }

    /** {@inheritDoc} */
    public synchronized MemoryCache put(String key, MemoryCache cache)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (cache == null)
        {
            String msg = Logging.getMessage("nullValue.CacheIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.caches.putIfAbsent(key, cache);
    }

    /** {@inheritDoc} */
    public synchronized boolean contains(String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.caches.containsKey(key);
    }

    /** {@inheritDoc} */
    public synchronized void clear()
    {
        for (MemoryCache cache : this.caches.values())
        {
            cache.clear();
        }

        this.caches.clear();
    }

    /** {@inheritDoc} */
    public Collection<PerformanceStatistic> getPerformanceStatistics()
    {
        if (this.performanceStatistics == null)
            this.performanceStatistics = new ArrayList<PerformanceStatistic>();
        this.performanceStatistics.clear();

        for (MemoryCache cache : this.caches.values())
        {
            this.performanceStatistics.add(
                new PerformanceStatistic(PerformanceStatistic.MEMORY_CACHE, "Cache Size (Kb): " + cache.getName(),
                    cache.getUsedCapacity() / 1000));
        }

        return this.performanceStatistics;
    }
}
