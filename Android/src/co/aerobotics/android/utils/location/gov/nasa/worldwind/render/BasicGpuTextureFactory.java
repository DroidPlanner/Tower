/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.render;

import android.graphics.*;
import android.opengl.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.WorldWind;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Matrix;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.dds.DDSTextureReader;

import java.io.*;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Matrix;

/**
 * @author dcollins
 * @version $Id$
 */
public class BasicGpuTextureFactory implements GpuTextureFactory
{
    protected static final boolean DEFAULT_GENERATE_MIPMAP = true;
    protected static final int DEFAULT_MARK_LIMIT = 1024;

    public BasicGpuTextureFactory()
    {
    }

    public static GpuTextureData createTextureData(String key, Object source, AVList params)
    {
        if (WWUtil.isEmpty(key))
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(source))
        {
            String msg = Logging.getMessage("nullValue.SourceIsNull");
            throw new IllegalArgumentException(msg);
        }

        GpuTextureFactory factory = (GpuTextureFactory) WorldWind.createConfigurationComponent(key);
        return factory.createTextureData(source, params);
    }

    public static GpuTexture createTexture(String key, DrawContext dc, GpuTextureData textureData, AVList params)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(key))
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            throw new IllegalArgumentException(msg);
        }

        if (textureData == null)
        {
            String msg = Logging.getMessage("nullValue.TextureDataIsNull");
            throw new IllegalArgumentException(msg);
        }

        GpuTextureFactory factory = (GpuTextureFactory) WorldWind.createConfigurationComponent(key);
        return factory.createTexture(dc, textureData, params);
    }

    /** {@inheritDoc} */
    public GpuTextureData createTextureData(Object source, AVList params)
    {
        if (WWUtil.isEmpty(source))
        {
            String msg = Logging.getMessage("nullValue.SourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        GpuTextureData data = null;

        try
        {
            if (source instanceof Bitmap)
            {
                data = this.doCreateFromBitmap((Bitmap) source);
            }
            else
            {
                // Attempt to open the source as an InputStream. This handle URLs, Files, InputStreams, a String
                // containing a valid URL, a String path to a file on the local file system, and a String path to a
                // class path resource.
                InputStream stream = WWIO.openStream(source);
                try
                {
                    if (stream != null)
                    {
                        // Wrap the stream in a BufferedInputStream to provide the mark/reset capability required to
                        // avoid destroying the stream when it is read more than once. BufferedInputStream also improves
                        // file read performance.
                        if (!(stream instanceof BufferedInputStream))
                            stream = new BufferedInputStream(stream);
                        data = this.doCreateFromStream(stream);
                    }
                }
                finally
                {
                    WWIO.closeStream(stream, source.toString()); // This method call is benign if the stream is null.
                }
            }
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("GpuTextureFactory.TextureDataCreationFailed", source);
            Logging.error(msg);
        }

        return data;
    }

    /** {@inheritDoc} */
    public GpuTexture createTexture(DrawContext dc, GpuTextureData textureData, AVList params)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            throw new IllegalArgumentException(msg);
        }

        if (textureData == null)
        {
            String msg = Logging.getMessage("nullValue.TextureDataIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        GpuTexture texture = null;

        try
        {
            if (textureData.getBitmapData() != null)
            {
                texture = this.doCreateFromBitmapData(dc, textureData, params);
            }
            else if (textureData.getCompressedData() != null)
            {
                texture = this.doCreateFromCompressedData(dc, textureData, params);
            }
            else
            {
                String msg = Logging.getMessage("generic.TextureDataUnrecognized", textureData);
                Logging.error(msg);
            }
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("GpuTextureFactory.TextureCreationFailed", textureData);
            Logging.error(msg);
        }

        return texture;
    }

    protected GpuTextureData doCreateFromBitmap(Bitmap bitmap)
    {
        return GpuTextureData.fromBitmap(bitmap, this.estimateMemorySize(bitmap));
    }

    protected GpuTextureData doCreateFromStream(InputStream stream)
    {
        GpuTextureData data = null;
        try
        {
            stream.mark(DEFAULT_MARK_LIMIT);

            DDSTextureReader ddsReader = new DDSTextureReader();
            data = ddsReader.read(stream);
            if (data != null)
                return data;

            stream.reset();

            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            return bitmap != null ? GpuTextureData.fromBitmap(bitmap, this.estimateMemorySize(bitmap)) : null;
        }
        catch (IOException e)
        {
            // TODO
        }

        return data;
    }

    protected GpuTexture doCreateFromBitmapData(DrawContext dc, GpuTextureData data, AVList params)
        throws Exception
    {
        Bitmap bitmap = data.getBitmapData().bitmap;

        Boolean generateMipmap = params != null ? (Boolean) params.getValue(AVKey.GENERATE_MIPMAP) : null;
        if (generateMipmap == null)
            generateMipmap = DEFAULT_GENERATE_MIPMAP;

        int[] texture = new int[1];
        try
        {
            GLES20.glGenTextures(1, texture, 0);
            if (texture[0] <= 0)
            {
                String msg = Logging.getMessage("GL.UnableToCreateObject", Logging.getMessage("term.Texture"));
                Logging.error(msg);
                return null;
            }

            // OpenGL ES provides support for non-power-of-two textures, including its associated mipmaps, provided that
            // the s and t wrap modes are both GL_CLAMP_TO_EDGE.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                generateMipmap ? GLES20.GL_LINEAR_MIPMAP_LINEAR : GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            if (generateMipmap)
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
        catch (Exception e)
        {
            GLES20.glDeleteTextures(1, texture, 0);
            throw e;
        }
        finally
        {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        return new GpuTexture(GLES20.GL_TEXTURE_2D, texture[0], bitmap.getWidth(), bitmap.getHeight(),
            data.getSizeInBytes(), this.createVerticalFlipTransform());
    }

    @SuppressWarnings( {"UnusedParameters"})
    protected GpuTexture doCreateFromCompressedData(DrawContext dc, GpuTextureData data, AVList params) throws Exception
    {
        int format = data.getCompressedData().format;
        GpuTextureData.MipmapData[] levelData = data.getCompressedData().levelData;

        int[] texture = new int[1];
        try
        {
            GLES20.glGenTextures(1, texture, 0);
            if (texture[0] <= 0)
            {
                String msg = Logging.getMessage("GL.UnableToCreateObject", Logging.getMessage("term.Texture"));
                Logging.error(msg);
                return null;
            }

            // OpenGL ES provides support for non-power-of-two textures, including its associated mipmaps, provided that
            // the s and t wrap modes are both GL_CLAMP_TO_EDGE.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                levelData.length > 1 ? GLES20.GL_LINEAR_MIPMAP_LINEAR : GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            for (int levelNum = 0; levelNum < levelData.length; levelNum++)
            {
                GpuTextureData.MipmapData level = levelData[levelNum];
                GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, levelNum, format, level.width, level.height, 0,
                    level.buffer.remaining(), level.buffer);
            }
        }
        catch (Exception e)
        {
            GLES20.glDeleteTextures(1, texture, 0);
            throw e;
        }
        finally
        {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        return new GpuTexture(GLES20.GL_TEXTURE_2D, texture[0], levelData[0].width, levelData[0].height,
            data.getSizeInBytes(), this.createVerticalFlipTransform());
    }

    @SuppressWarnings( {"UnusedParameters"})
    protected Matrix createVerticalFlipTransform()
    {
        // Android places its graphics coordinate origin in the upper left corner, with the y axis pointing down. This
        // means that bitmap data must be interpreted as starting in the upper left corner. Since World Wind and OpenGL
        // expect the coordinate origin to be in the lower left corner, and interpret textures as having their data
        // origin in the lower left corner, images loaded by the Android BitmapFactory must always be flipped
        // vertically. Flipping an image vertically is accomplished by multiplying scaling the t-coordinate by -1 then
        // translating the t-coordinate by -1. We have pre-computed the product of the scaling and translation matrices
        // and stored the result inline here to avoid unnecessary matrix allocations and multiplications. The matrix
        // below is equivalent to the following:
        //
        // Matrix scale = Matrix.fromIdentity().setScale(1, -1, 1);
        // Matrix trans = Matrix.fromIdentity().setTranslation(0, -1, 0);
        // Matrix internalTransform = Matrix.fromIdentity();
        // internalTransform.multiplyAndSet(scale);
        // internalTransform.multiplyAndSet(trans);
        // return internalTransform;

        return new Matrix(
            1, 0, 0, 0,
            0, -1, 0, 1,
            0, 0, 1, 0,
            0, 0, 0, 1);
    }

    protected long estimateMemorySize(Bitmap bitmap)
    {
        int internalFormat = GLUtils.getInternalFormat(bitmap);

        if (internalFormat == GLES20.GL_ALPHA || internalFormat == GLES20.GL_LUMINANCE)
        {
            // Alpha and luminance pixel data is always stored as 1 byte per pixel. See OpenGL ES Specification, version 2.0.25,
            // section 3.6.2, table 3.4.
            return bitmap.getWidth() * bitmap.getHeight();
        }
        else if (internalFormat == GLES20.GL_LUMINANCE_ALPHA)
        {
            // Luminance-alpha pixel data is always stored as 2 bytes per pixel. See OpenGL ES Specification,
            // version 2.0.25, section 3.6.2, table 3.4.
            return 2 * bitmap.getWidth() * bitmap.getHeight(); // Type must be GL_UNSIGNED_BYTE.
        }
        else if (internalFormat == GLES20.GL_RGB)
        {
            // RGB pixel data is stored as either 2 or 3 bytes per pixel, depending on the type used during texture
            // image specification. See OpenGL ES Specification, version 2.0.25, section 3.6.2, table 3.4.
            int type = GLUtils.getType(bitmap);
            // Default to type GL_UNSIGNED_BYTE.
            int bpp = (type == GLES20.GL_UNSIGNED_SHORT_5_6_5 ? 2 : 3);
            return bpp * bitmap.getWidth() * bitmap.getHeight();
        }
        else // Default to internal format GL_RGBA.
        {
            // RGBA pixel data is stored as either 2 or 4 bytes per pixel, depending on the type used during texture
            // image specification. See OpenGL ES Specification, version 2.0.25, section 3.6.2, table 3.4.
            int type = GLUtils.getType(bitmap);
            // Default to type GL_UNSIGNED_BYTE.
            int bpp = (type == GLES20.GL_UNSIGNED_SHORT_4_4_4_4 || type == GLES20.GL_UNSIGNED_SHORT_5_5_5_1) ? 2 : 4;
            return bpp * bitmap.getWidth() * bitmap.getHeight();
        }
    }
}
