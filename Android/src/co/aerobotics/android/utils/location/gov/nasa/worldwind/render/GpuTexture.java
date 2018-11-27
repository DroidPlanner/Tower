/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.render;

import android.opengl.GLES20;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.Disposable;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.Cacheable;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Matrix;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class GpuTexture implements Cacheable, Disposable
{
    protected int target;
    protected int textureId;
    protected int width;
    protected int height;
    protected long estimatedMemorySize;
    protected Matrix internalTransform;

    public GpuTexture(int target, int textureId, int width, int height, long estimatedMemorySize,
        Matrix texCoordMatrix)
    {
        if (target != GLES20.GL_TEXTURE_2D
            && target != GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X
            && target != GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
            && target != GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
            && target != GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X
            && target != GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y
            && target != GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z)
        {
            String msg = Logging.getMessage("generic.TargetIsInvalid", target);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (textureId <= 0)
        {
            String msg = Logging.getMessage("GL.GLObjectIsInvalid", textureId);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

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

        if (estimatedMemorySize <= 0)
        {
            String msg = Logging.getMessage("generic.SizeIsInvalid", estimatedMemorySize);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.target = target;
        this.textureId = textureId;
        this.width = width;
        this.height = height;
        this.estimatedMemorySize = estimatedMemorySize;
        this.internalTransform = texCoordMatrix;
    }

    public int getTarget()
    {
        return this.target;
    }

    public int getTextureId()
    {
        return this.textureId;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public long getSizeInBytes()
    {
        return this.estimatedMemorySize;
    }

    public void bind()
    {
        GLES20.glBindTexture(this.target, this.textureId);
    }

    public void dispose()
    {
        int[] textures = new int[] {this.textureId};
        GLES20.glDeleteTextures(1, textures, 0);
    }

    public void applyInternalTransform(DrawContext dc, Matrix matrix)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.internalTransform != null)
            matrix.multiplyAndSet(this.internalTransform);
    }
}
