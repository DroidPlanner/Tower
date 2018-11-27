/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

import android.opengl.GLES20;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.Disposable;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

/**
 * Provides the interface for caching of OpenGL resources that are stored on or registered with a GL context. This cache
 * maintains a map of resources that fit within a specifiable memory capacity. If adding a resource would exceed this
 * reduced to the "low water" size in this case (see {@link #setLowWater(long)}.
 * <p/>
 * When a resource is removed from the cache, and if it is a recognized OpenGL resource -- a texture, a list of vertex
 * buffer IDs, a list of display list IDs, etc. -- and there is a current Open GL context, the appropriate glDelete
 * function is called to de-register the resource with the GPU. If there is no current OpenGL context the resource is
 * not deleted and will likely remain allocated on the GPU until the GL context is destroyed.
 *
 * @author tag
 * @version $Id$
 */
public class BasicGpuResourceCache implements GpuResourceCache
{
    public static class CacheEntry implements Cacheable
    {
        protected final String resourceType;
        protected final Object resource;
        protected long resourceSize;

        public CacheEntry(Object resource, String resourceType)
        {
            this.resource = resource;
            this.resourceType = resourceType;
        }

        public CacheEntry(Object resource, String resourceType, long size)
        {
            this.resource = resource;
            this.resourceType = resourceType;
            this.resourceSize = size;
        }

        public long getSizeInBytes()
        {
            return this.resourceSize;
        }
    }

    protected final BasicMemoryCache resources;

    public BasicGpuResourceCache(long loWater, long hiWater)
    {
        this.resources = new BasicMemoryCache(loWater, hiWater);
        this.resources.setName("GPU Resource Cache");
        this.resources.addCacheListener(new MemoryCache.CacheListener()
        {
            public void entryRemoved(Object key, Object clientObject)
            {
                onEntryRemoved(key, clientObject);
            }

            public void removalException(Throwable e, Object key, Object clientObject)
            {
                String msg = Logging.getMessage("GL.ExceptionRemovingCachedGpuResource", clientObject);
                Logging.info(msg, e);
            }
        });
    }

    @SuppressWarnings( {"UnusedParameters", "StringEquality"})
    protected void onEntryRemoved(Object key, Object clientObject)
    {
        // If this entry is removed as a result of GpuResourceCache.clear being called because the EGL context has
        // changed, the currently active context is not the one used to create the Gpu resources in the cache. In this
        // case, the cache is emptied and the GL silently ignores deletion of resource names that it does not recognize.

        if (!(clientObject instanceof CacheEntry)) // shouldn't be null or wrong type, but check anyway
            return;

        CacheEntry entry = (CacheEntry) clientObject;

        if (entry.resource instanceof Disposable)
        {
            // Dispose a GPU resource when it leaves the cache.
            ((Disposable) entry.resource).dispose();
        }
        else if (entry.resourceType == VBO_BUFFERS)
        {
            int[] ids = (int[]) entry.resource;
            GLES20.glDeleteBuffers(ids.length, ids, 0);
        }
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

        CacheEntry entry = (CacheEntry) this.resources.get(key);
        return entry != null ? entry.resource : null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings( {"StringEquality"})
    public GpuProgram getProgram(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        CacheEntry entry = (CacheEntry) this.resources.get(key);
        return entry != null && entry.resourceType == PROGRAM ? (GpuProgram) entry.resource : null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings( {"StringEquality"})
    public GpuTexture getTexture(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        CacheEntry entry = (CacheEntry) this.resources.get(key);
        return entry != null && entry.resourceType == TEXTURE ? (GpuTexture) entry.resource : null;
    }

    /** {@inheritDoc} */
    public void put(Object key, Object resource, String resourceType, long size)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (resource == null)
        {
            String msg = Logging.getMessage("nullValue.ResourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(resourceType))
        {
            String msg = Logging.getMessage("nullValue.ResourceTypeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (size < 1)
        {
            String msg = Logging.getMessage("MemoryCache.SizeIsLessThanOne", size);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        long cap = this.getCapacity();

        if (size > cap)
        {
            String msg = Logging.getMessage("MemoryCache.SizeIsLargerThanCapacity", size, cap);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        CacheEntry entry = this.createCacheEntry(resource, resourceType, size);
        this.resources.put(key, entry);
    }

    /** {@inheritDoc} */
    public void put(Object key, GpuProgram program)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (program == null)
        {
            String msg = Logging.getMessage("nullValue.ProgramIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        CacheEntry entry = this.createCacheEntry(program, PROGRAM);
        this.resources.put(key, entry);
    }

    /** {@inheritDoc} */
    public void put(Object key, GpuTexture texture)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (texture == null)
        {
            String msg = Logging.getMessage("nullValue.TextureIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        CacheEntry entry = this.createCacheEntry(texture, TEXTURE);
        this.resources.put(key, entry);
    }

    protected CacheEntry createCacheEntry(Object resource, String resourceType)
    {
        CacheEntry entry = new CacheEntry(resource, resourceType);
        entry.resourceSize = this.computeEntrySize(entry);

        return entry;
    }

    protected CacheEntry createCacheEntry(Object resource, String resourceType, long size)
    {
        CacheEntry entry = new CacheEntry(resource, resourceType, size);
        entry.resourceSize = size;

        return entry;
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

        return this.resources.contains(key);
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

        this.resources.remove(key);
    }

    /** {@inheritDoc} */
    public void clear()
    {
        this.resources.clear();
    }

    /** {@inheritDoc} */
    public int getNumObjects()
    {
        return this.resources.getNumObjects();
    }

    /** {@inheritDoc} */
    public long getCapacity()
    {
        return this.resources.getCapacity();
    }

    /** {@inheritDoc} */
    public long getUsedCapacity()
    {
        return this.resources.getUsedCapacity();
    }

    /** {@inheritDoc} */
    public long getFreeCapacity()
    {
        return this.resources.getFreeCapacity();
    }

    /** {@inheritDoc} */
    public void setCapacity(long newCapacity)
    {
        this.resources.setCapacity(newCapacity);
    }

    /** {@inheritDoc} */
    public long getLowWater()
    {
        return this.resources.getLowWater();
    }

    /** {@inheritDoc} */
    public void setLowWater(long loWater)
    {
        this.resources.setLowWater(loWater);
    }

    @SuppressWarnings( {"StringEquality"})
    protected long computeEntrySize(CacheEntry entry)
    {
        if (entry.resource instanceof Cacheable)
            return ((Cacheable) entry.resource).getSizeInBytes();

        return 0;
    }
}
