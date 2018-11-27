/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.render;

import android.graphics.Bitmap;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.Cacheable;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author dcollins
 * @version $Id$
 */
public class GpuTextureData implements Cacheable
{
    public static class BitmapData
    {
        public final Bitmap bitmap;

        public BitmapData(Bitmap bitmap)
        {
            if (bitmap == null)
            {
                String msg = Logging.getMessage("nullValue.BitmapIsNull");
                Logging.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.bitmap = bitmap;
        }
    }

    public static class CompressedData
    {
        public final int format;
        public final MipmapData[] levelData;

        public CompressedData(int format, MipmapData[] levelData)
        {
            if (levelData == null || levelData.length == 0)
            {
                String msg = Logging.getMessage("nullValue."); // TODO
                Logging.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.format = format;
            this.levelData = levelData;
        }
    }

    public static class MipmapData
    {
        public final int width;
        public final int height;
        public final ByteBuffer buffer;

        public MipmapData(int width, int height, ByteBuffer buffer)
        {
            if (width < 0)
            {
                String msg = Logging.getMessage("generic.WidthIsInvalid", width);
                Logging.error(msg);
                throw new IllegalArgumentException(msg);
            }

            if (height < 0)
            {
                String msg = Logging.getMessage("generic.HeightIsInvalid", height);
                Logging.error(msg);
                throw new IllegalArgumentException(msg);
            }

            if (buffer == null)
            {
                String msg = Logging.getMessage("nullValue.BufferIsNull");
                Logging.error(msg);
                throw new IllegalArgumentException(msg);
            }

            this.width = width;
            this.height = height;
            this.buffer = buffer;
        }
    }

    protected BitmapData bitmapData;
    protected CompressedData compressedData;
    protected long estimatedMemorySize;

    public static GpuTextureData fromBitmap(Bitmap bitmap, long estimatedMemorySize)
    {
        if (bitmap == null)
        {
            String msg = Logging.getMessage("nullValue.BitmapIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (estimatedMemorySize <= 0)
        {
            String msg = Logging.getMessage("generic.SizeIsInvalid", estimatedMemorySize);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        GpuTextureData textureData = new GpuTextureData();
        textureData.bitmapData = new BitmapData(bitmap);
        textureData.estimatedMemorySize = estimatedMemorySize;

        return textureData;
    }

    public static GpuTextureData fromCompressedData(int format, MipmapData[] levelData, long estimatedMemorySize)
    {
        if (levelData == null || levelData.length == 0)
        {
            String msg = Logging.getMessage("nullValue."); // TODO
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (estimatedMemorySize <= 0)
        {
            String msg = Logging.getMessage("generic.SizeIsInvalid", estimatedMemorySize);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        GpuTextureData textureData = new GpuTextureData();
        textureData.compressedData = new CompressedData(format, levelData);
        textureData.estimatedMemorySize = estimatedMemorySize;

        return textureData;
    }

    protected GpuTextureData()
    {
    }

    public BitmapData getBitmapData()
    {
        return this.bitmapData;
    }

    public CompressedData getCompressedData()
    {
        return this.compressedData;
    }

    public long getSizeInBytes()
    {
        return this.estimatedMemorySize;
    }
}
