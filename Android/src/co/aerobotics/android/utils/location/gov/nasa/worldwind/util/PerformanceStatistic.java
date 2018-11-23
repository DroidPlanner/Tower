/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

/**
 * @author dcollins
 * @version $Id$
 */
public class PerformanceStatistic
{
    public static final String FRAME_TIME = "gov.nasa.worldwind.perfstat.FrameTime";
    public static final String GPU_RESOURCE_CACHE = "gov.nasa.worldwind.perfstat.GpuResourceCache";
    public static final String IMAGE_TILE_COUNT = "gov.nasa.worldwind.perfstat.ImageTileCount";
    public static final String JVM_HEAP = "gov.nasa.worldwind.perfstat.JvmHeap";
    public static final String JVM_HEAP_USED = "gov.nasa.worldwind.perfstat.JvmHeapUsed";
    public static final String LAYER_FRAME_TIME = "gov.nasa.worldwind.perfstat.LayerFrameTime";
    public static final String LAYER_PICK_TIME = "gov.nasa.worldwind.perfstat.LayerFrameTime";
    public static final String MEMORY_CACHE = "gov.nasa.worldwind.perfstat.MemoryCache";
    public static final String TERRAIN_FRAME_TIME = "gov.nasa.worldwind.perfstat.TerrainFrameTime";
    public static final String TERRAIN_PICK_TIME = "gov.nasa.worldwind.perfstat.TerrainPickTime";
    public static final String TERRAIN_TILE_COUNT = "gov.nasa.worldwind.perfstat.TerrainTileCount";

    protected String key;
    protected String displayName;
    protected Object value;

    public PerformanceStatistic(String key, String displayName, Object value)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (displayName == null)
        {
            String msg = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (value == null)
        {
            String msg = Logging.getMessage("nullValue.ValueIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.key = key;
        this.displayName = displayName;
        this.value = value;
    }

    public String getKey()
    {
        return this.key;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public Object getValue()
    {
        return this.value;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        PerformanceStatistic that = (PerformanceStatistic) o;
        return this.key.equals(that.key) && this.displayName.equals(that.displayName) && this.value.equals(that.value);
    }

    public int hashCode()
    {
        int result;
        result = this.key.hashCode();
        result = 31 * result + this.displayName.hashCode();
        result = 31 * result + this.value.hashCode();
        return result;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.displayName);
        sb.append(" ");
        sb.append(this.value);

        return sb.toString();
    }
}
