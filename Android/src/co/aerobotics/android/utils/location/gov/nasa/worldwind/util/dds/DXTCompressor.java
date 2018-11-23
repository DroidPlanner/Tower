/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.util.dds;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * The <code>DXTCompressor</code> interface will compress an in-memory image using one of the DXT block compression
 * schemes. The details of each block compression scheme is handled by the implementation.
 *
 * @author dcollins
 * @version $Id$
 */
public interface DXTCompressor
{
    /**
     * Returns the DXT format constant associated with this compressor.
     *
     * @return DXT format for this compressor.
     *
     * @see DDSConstants
     */
    int getDXTFormat();

    /**
     * Returns the compressed size in bytes of the specified <code>image</code>.
     *
     * @param image      the image to compute the compressed size for.
     * @param attributes the attributes that may affect the compressed size.
     *
     * @return compressed size in bytes of the specified image.
     *
     * @throws IllegalArgumentException if either <code>image</code> or <code>attributes</code> is null.
     */
    int getCompressedSize(Bitmap image, DXTCompressionAttributes attributes);

    /**
     * Encodes the specified <code>image</code> image into a compressed DXT codec, and writes the compressed bytes to
     * the specified <code>buffer</code>. The buffer should be allocated with enough space to hold the compressed
     * output. The correct size should be computed by calling <code>getCompressedSize(image, attributes)</code>.
     *
     * @param image      the image to compress.
     * @param attributes the attributes that may affect the compression.
     * @param buffer     the buffer that will receive the compressed output.
     *
     * @throws IllegalArgumentException if any of <code>image</code>, <code>attributes</code>, or <code>buffer</code>
     *                                  are null.
     * @see #getCompressedSize(Bitmap, DXTCompressionAttributes)
     */
    void compressImage(Bitmap image, DXTCompressionAttributes attributes, ByteBuffer buffer);
}
