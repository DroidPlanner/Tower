/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.GpuProgram;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.GpuTexture;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.*;

/**
 * Implements a cache of OpenGL resources that are stored on the GPU or registered with it. This includes texture,
 * vertex buffer and display list resource IDs returned by the corresponding OpenGL functions. The cache holds the most
 * recently used resources that fit within its capacity.
 *
 * @author tag
 * @version $Id$
 */
public interface GpuResourceCache
{
    /**
     * Identifies resources as Gpu programs. Corresponding object must be of type {@link
     * GpuProgram}.
     */
    public static final String PROGRAM = "gov.nasa.worldwind.cache.GpuResourceCache.Program";

    /**
     * Identifies resources as Gpu textures. Corresponding object must be of type {@link
     * GpuTexture}.
     */
    public static final String TEXTURE = "gov.nasa.worldwind.cache.GpuResourceCache.Texture";
    /**
     * Identifies resources as Vertex Buffer IDs. Corresponding object must be of type int[] and contain the VBO
     * resource IDs.
     */
    public static final String VBO_BUFFERS = "gov.nasa.worldwind.cache.GpuResourceCache.VboBuffers";

    /**
     * Finds and returns a resource from this cache.
     *
     * @param key the resource's key.
     *
     * @return the resource associated with the key, or null if the key is not found in the cache.
     *
     * @throws IllegalArgumentException if the key is null.
     */
    Object get(Object key);

    /**
     * Finds and returns a program resource from this cache.
     *
     * @param key the program resource's key.
     *
     * @return the  program resource associated with the key, or null if the key is not found in the cache.
     *
     * @throws IllegalArgumentException if the key is null.
     */
    GpuProgram getProgram(Object key);

    /**
     * Finds and returns a texture resource from this cache.
     *
     * @param key the texture resource's key.
     *
     * @return the  texture resource associated with the key, or null if the key is not found in the cache.
     *
     * @throws IllegalArgumentException if the key is null.
     */
    GpuTexture getTexture(Object key);

    /**
     * Adds a new resource to this cache.
     *
     * @param key          the  key identifying the resource.
     * @param resource     the resource cached.
     * @param resourceType the type of resource, one of the resource-type constants described above.
     * @param size         the size of the resource, expressed as the number of bytes it requires on the GPU.
     */
    void put(Object key, Object resource, String resourceType, long size);

    /**
     * Add a resource to this cache.
     *
     * @param key     the key identifying the resource.
     * @param program the resource to add to this cache.
     *
     * @throws IllegalArgumentException if either argument is null.
     */
    void put(Object key, GpuProgram program);

    /**
     * Add a resource to this cache.
     *
     * @param key     the key identifying the resource.
     * @param texture the resource to add to this cache.
     *
     * @throws IllegalArgumentException if either argument is null.
     */
    void put(Object key, GpuTexture texture);

    /**
     * Indicates whether a resource is in the cache.
     *
     * @param key the resource's key.
     *
     * @return true if the resource is in the cache, otherwise false.
     */
    boolean contains(Object key);

    /**
     * Removes a resource from this cache.
     *
     * @param key the resource's key.
     *
     * @throws IllegalArgumentException if the key is null.
     */
    void remove(Object key);

    /** Removes all entries from this cache. */
    void clear();

    /**
     * Indicates the number of resources in this cache.
     *
     * @return the number of resources in this cache.
     */
    int getNumObjects();

    /**
     * Inticates this cache's capacity in bytes.
     *
     * @return this cache's capacity in bytes.
     */
    long getCapacity();

    /**
     * Indicates the amount of memory used by cached objects in this cache.
     *
     * @return the number of bytes of memory used by objects in this cache, as determined by the size associated with
     *         each resource.
     *
     * @see #getCapacity()
     */
    long getUsedCapacity();

    /**
     * Indicates this cache's memory capacity not used by its cached objects.
     *
     * @return the number of bytes of this cache's memory capacity not used by its cached objects.
     *
     * @see #getCapacity()
     */
    long getFreeCapacity();

    /**
     * Specifies this cache's capacity, the amount of memory the cache manages. When new resources are added to this
     * cache and would exceed the cache's capacity, the cache's least recently used resources are removed from it until
     * its low water level is reached.
     *
     * @param newCapacity the number of bytes allowed for the cache's resources. Values less than or equal to 0 are
     *                    ignored and cause no change to this cache's capacity.
     *
     * @see #setLowWater(long)
     */
    void setCapacity(long newCapacity);

    /**
     * Indicates the cache's low water size.
     *
     * @return the cache's low water size, in bytes.
     *
     * @see #setLowWater(long)
     */
    long getLowWater();

    /**
     * Specifies the low water size for this cache. When resources added to the cache would exceed the cache's capacity,
     * existing resources are removed until the amount of memory used is at or below the low water size.
     *
     * @param loWater the size to reduce this cache to when added resources would exceed the cache's capacity. Values
     *                less than or equal to 0 are ignored and cause no change to this cache's low water size.
     *
     * @see #setCapacity(long)
     * @see #remove(Object)
     */
    void setLowWater(long loWater);
}