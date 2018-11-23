/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.PerformanceStatistic;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id$
 */
public interface MemoryCacheSet
{
    MemoryCache get(String key);

    MemoryCache put(String key, MemoryCache cache);

    boolean contains(String key);

    void clear();

    Collection<PerformanceStatistic> getPerformanceStatistics();
}
