/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.util.dds;

import android.graphics.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

/**
 * DDSCompressor converts in-memory images into a DDS file encoded with one of the DXT block compression algorithms. If
 * the caller wants to encode using a certain type of DXT compression, DDSCompressor provides the appropriate methods to
 * do that. Otherwise, DDSCompressor chooses the DXT compression scheme that best suits the source image.
 * <p/>
 * Each compression method accepts a reference to a {@link DXTCompressionAttributes}. This compressor performs the
 * appropriate actions according to the attributes, such as building mip maps and converting the source image to a
 * premultiplied alpha format.
 *
 * @author dcollins
 * @version $Id$
 */
public class DDSCompressor
{
    /** Creates a new DDSCompressor, but otherwise does nothing. */
    public DDSCompressor()
    {
    }

    /**
     * Convenience method to convert the specified image <code>stream</code> to DDS according to the specified
     * compression <code>attributes</code>. The <code>stream</code> must be readable by {@link
     * BitmapFactory#decodeStream BitmapFactory.decodeStream}. Once the <code>stream</code> is read, this is equivalent
     * to calling {@link #compressImage(Bitmap, DXTCompressionAttributes)} with the Bitmap created with the specified
     * <code>attributes</code>. This returns null if the <code>stream</code> is not in a format understood by
     * BitmapFactory.
     *
     * @param inputStream image stream to convert to the DDS file format.
     * @param attributes  attributes that control the compression.
     *
     * @return little endian ordered ByteBuffer containing the DDS file bytes, or null if the <code>stream</code> is not
     *         in a format understood by BitmapFactory.
     *
     * @throws IllegalArgumentException if either the <code>stream</code> or the <code>attributes</code> are null.
     */
    public static java.nio.ByteBuffer compressImageStream(java.io.InputStream inputStream,
        DXTCompressionAttributes attributes)
    {
        if (inputStream == null)
        {
            String message = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Bitmap image = BitmapFactory.decodeStream(inputStream);
        if (image == null)
        {
            return null;
        }

        DDSCompressor compressor = new DDSCompressor();
        return compressor.compressImage(image, attributes);
    }

    /**
     * Convenience method to convert the specified <code>imageBuffer</code> to DDS according to the specified
     * compression <code>attributes</code>. The bytes in <code>imageBuffer</code> must be readable by {@link
     * BitmapFactory#decodeStream BitmapFactory.decodeStream}. Once the image data is read, this is equivalent to
     * calling {@link #compressImage(Bitmap, DXTCompressionAttributes)} with the Bitmap created with the specified
     * <code>attributes</code>. This returns null if the bytes in <code>imageBuffer</code> are not in a format
     * understood by BitmapFactory.
     *
     * @param imageBuffer image file data to convert to the DDS file format.
     * @param attributes  attributes that control the compression.
     *
     * @return little endian ordered ByteBuffer containing the DDS file bytes, or null if the <code>imageBuffer</code>
     *         is not in a format understood by BitmapFactory.
     *
     * @throws IllegalArgumentException if either <code>imageBuffer</code> or <code>attributes</code> are null.
     */
    public static java.nio.ByteBuffer compressImageBuffer(java.nio.ByteBuffer imageBuffer,
        DXTCompressionAttributes attributes)
    {
        if (imageBuffer == null)
        {
            String message = Logging.getMessage("nullValue.Image");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        java.io.InputStream inputStream = WWIO.getInputStreamFromByteBuffer(imageBuffer);
        return compressImageStream(inputStream, attributes);
    }

    /**
     * Convenience method to convert the specified <code>imageBuffer</code> to DDS according to the default attributes.
     * The bytes in <code>imageBuffer</code> must be readable by {@link BitmapFactory#decodeStream
     * BitmapFactory.decodeStream}. Once the image data is read, this is equivalent to calling {@link
     * #compressImage(Bitmap)} with the Bitmap created by BitmapFactory. This returns null if the bytes
     * in<code>imageBuffer</code> are not in a format understood by BitmapFactory.
     *
     * @param imageBuffer image file data to convert to the DDS file format.
     *
     * @return little endian ordered ByteBuffer containing the DDS file bytes, or null if the <code>imageBuffer</code>
     *         is not in a format understood by BitmapFactory.
     *
     * @throws IllegalArgumentException if <code>imageBuffer</code> is null.
     */
    public static java.nio.ByteBuffer compressImageBuffer(java.nio.ByteBuffer imageBuffer)
    {
        if (imageBuffer == null)
        {
            String message = Logging.getMessage("nullValue.Image");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        return compressImageBuffer(imageBuffer, getDefaultCompressionAttributes());
    }

    /**
     * Convenience method to convert the specified <code>image</code> to DDS according to the default attributes. This
     * chooses the DXT compression format best suited for the image type.
     *
     * @param image image to convert to the DDS file format.
     *
     * @return little endian ordered ByteBuffer containing the dds file bytes.
     *
     * @throws IllegalArgumentException if <code>image</code> is null, or if <code>image</code> has non power of two
     *                                  dimensions.
     */
    public static java.nio.ByteBuffer compressImage(Bitmap image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (!WWMath.isPowerOfTwo(image.getWidth()) || !WWMath.isPowerOfTwo(image.getHeight()))
        {
            String message = Logging.getMessage("generic.InvalidImageSize", image.getWidth(), image.getHeight());
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        DDSCompressor compressor = new DDSCompressor();
        DXTCompressionAttributes attributes = getDefaultCompressionAttributes();
        return compressor.compressImage(image, attributes);
    }

    /**
     * Returns the default compression attributes. The default DXT compression attributes are defined as follows:
     * <table> <tr><th>Attribute</th><th>Value</th></tr> <tr><td>Build Mipmaps</td><td>true</td></tr>
     * <tr><td>Premultiply Alpha</td><td>true</td></tr> <tr><td>DXT Format</td><td>Let DDSCompressor choose optimal
     * format.</td></tr> <tr><td>Enable DXT1 Alpha</td><td>false</td></tr> <tr><td>DXT1 Alpha
     * Threshold</td><td>128</td></tr> <tr><td>Compression Algorithm</td><td>Euclidean Distance</td></tr> </table>
     *
     * @return the default compression attributes.
     */
    public static DXTCompressionAttributes getDefaultCompressionAttributes()
    {
        DXTCompressionAttributes attributes = new DXTCompressionAttributes();
        attributes.setBuildMipmaps(true); // Always build mipmaps.
        attributes.setPremultiplyAlpha(true); // Always create premultiplied alpha format files..
        attributes.setDXTFormat(0); // Allow the DDSCompressor to choose the appropriate DXT format.
        return attributes;
    }

    /**
     * Converts the specified <code>image</code> to DDS according to the <code>attributes</code>. If the caller
     * specified a DXT format in the attributes, then we return a compressor matching that format. Otherwise, we choose
     * one automatically from the image type. If no choice can be made from the image type, we default to using a DXT3
     * compressor.
     *
     * @param image      image to convert to the DDS file format.
     * @param attributes attributes that control the compression.
     *
     * @return buffer little endian ordered ByteBuffer containing the dds file bytes.
     *
     * @throws IllegalArgumentException if either <code>image</code> or <code>attributes</code> are null, or if
     *                                  <code>image</code> has non power of two dimensions.
     */
    public java.nio.ByteBuffer compressImage(Bitmap image, DXTCompressionAttributes attributes)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (!WWMath.isPowerOfTwo(image.getWidth()) || !WWMath.isPowerOfTwo(image.getHeight()))
        {
            String message = Logging.getMessage("generic.InvalidImageSize", image.getWidth(), image.getHeight());
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        DXTCompressor compressor = this.getDXTCompressor(image, attributes);
        return this.doCompressImage(compressor, image, attributes);
    }

    /**
     * Converts the specified <code>image</code> to DDS using the DXT1 codec, and otherwise according to the
     * <code>attributes</code>.
     *
     * @param image      image to convert to the DDS file format using the DXT1 codec.
     * @param attributes attributes that control the compression.
     *
     * @return buffer little endian ordered ByteBuffer containing the dds file bytes.
     *
     * @throws IllegalArgumentException if either <code>image</code> or <code>attributes</code> are null, or if
     *                                  <code>image</code> has non power of two dimensions.
     */
    public java.nio.ByteBuffer compressImageDXT1(Bitmap image, DXTCompressionAttributes attributes)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (!WWMath.isPowerOfTwo(image.getWidth()) || !WWMath.isPowerOfTwo(image.getHeight()))
        {
            String message = Logging.getMessage("generic.InvalidImageSize", image.getWidth(), image.getHeight());
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        DXT1Compressor compressor = new DXT1Compressor();
        return this.doCompressImage(compressor, image, attributes);
    }

    /**
     * Converts the specified <code>image</code> to DDS using the DXT3 codec, and otherwise according to the
     * <code>attributes</code>.
     *
     * @param image      image to convert to the DDS file format using the DXT3 codec.
     * @param attributes attributes that control the compression.
     *
     * @return buffer little endian ordered ByteBuffer containing the dds file bytes.
     *
     * @throws IllegalArgumentException if either <code>image</code> or <code>attributes</code> are null, or if
     *                                  <code>image</code> has non power of two dimensions.
     */
    public java.nio.ByteBuffer compressImageDXT3(Bitmap image, DXTCompressionAttributes attributes)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (!WWMath.isPowerOfTwo(image.getWidth()) || !WWMath.isPowerOfTwo(image.getHeight()))
        {
            String message = Logging.getMessage("generic.InvalidImageSize", image.getWidth(), image.getHeight());
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        DXT3Compressor compressor = new DXT3Compressor();
        return this.doCompressImage(compressor, image, attributes);
    }

    protected java.nio.ByteBuffer doCompressImage(DXTCompressor compressor, Bitmap image,
        DXTCompressionAttributes attributes)
    {
        // Create the DDS header structure that describes the specified image, compressor, and compression attributes.
        DDSHeader header = this.createDDSHeader(compressor, image, attributes);

        // Compute the DDS file size and mip map levels. If the attributes specify to build mip maps, then we compute
        // the total file size including mip maps, create a chain of mip map images, and update the DDS header to
        // describe the number of mip map levels. Otherwise, we compute the file size for a single image and do nothing
        // to the DDS header.
        Bitmap[] mipMapLevels = null;
        int fileSize = 4 + header.getSize();

        if (attributes.isBuildMipmaps())
        {
            mipMapLevels = this.buildMipMaps(image, attributes);
            for (Bitmap mipMapImage : mipMapLevels)
            {
                fileSize += compressor.getCompressedSize(mipMapImage, attributes);
            }

            header.setFlags(header.getFlags()
                | DDSConstants.DDSD_MIPMAPCOUNT);
            header.setMipMapCount(mipMapLevels.length);
        }
        else
        {
            fileSize += compressor.getCompressedSize(image, attributes);
        }

        // Create a little endian buffer that holds the bytes of the DDS file.
        java.nio.ByteBuffer buffer = this.createBuffer(fileSize);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Write the DDS magic number and DDS header to the file.
        buffer.putInt(DDSConstants.MAGIC);
        this.writeDDSHeader(header, buffer);

        // Write the compressed DXT blocks to the DDS file. If the attributes specify to build mip maps, then we write
        // each mip map level to the DDS file, starting with level 0 and ending with level N. Otherwise, we write a
        // single image to the DDS file.
        if (mipMapLevels == null)
        {
            compressor.compressImage(image, attributes, buffer);
        }
        else
        {
            for (Bitmap mipMapImage : mipMapLevels)
            {
                compressor.compressImage(mipMapImage, attributes, buffer);
            }
        }

        buffer.rewind();
        return buffer;
    }

    protected DXTCompressor getDXTCompressor(Bitmap image, DXTCompressionAttributes attributes)
    {
        // If the caller specified a DXT format in the attributes, then we return a compressor matching that format.
        // Otherwise, we choose one automatically from the image type. If no choice can be made from the image type,
        // we default to using a DXT3 compressor.

        if (attributes.getDXTFormat() == DDSConstants.D3DFMT_DXT1)
        {
            return new DXT1Compressor();
        }
        else if (attributes.getDXTFormat() == DDSConstants.D3DFMT_DXT2
            || attributes.getDXTFormat() == DDSConstants.D3DFMT_DXT3)
        {
            return new DXT3Compressor();
        }
        else if (!image.hasAlpha())
        {
            return new DXT1Compressor();
        }
        else
        {
            return new DXT3Compressor();
        }
    }

    protected java.nio.ByteBuffer createBuffer(int size)
    {
        return java.nio.ByteBuffer.allocateDirect(size);
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected Bitmap[] buildMipMaps(Bitmap image, DXTCompressionAttributes attributes)
    {
        // Build the mipmap chain using a premultiplied alpha image format. This is necessary to ensure that
        // transparent colors do not bleed into the opaque colors. For example, without premultiplied alpha the colors
        // in a totally transparent pixel may contribute when one mipmap level is filtered (with either a box or a
        // bilinear filter) to produce the pixels for the next level.
        //
        // The DXT color block extractor typically accessed Bitmap data via a call to getRGB(). This returns
        // a packed 8888 ARGB int, where the color components are known to be not premultiplied, and in the sRGB color
        // space. Therefore computing mipmaps in this way does not affect the rest of the DXT pipeline, unless color
        // data is accessed directly. In this case, such code would be responsible for recognizing the color model
        // (premultiplied) and behaving accordingly.

        Bitmap.Config mipmapImageType = Bitmap.Config.ARGB_8888;
        int maxLevel = ImageUtil.getMaxMipmapLevel(image.getWidth(), image.getHeight());

        return ImageUtil.buildMipmaps(image, mipmapImageType, maxLevel);
    }

    protected DDSHeader createDDSHeader(DXTCompressor compressor, Bitmap image, DXTCompressionAttributes attributes)
    {
        DDSPixelFormat pixelFormat = new DDSPixelFormat();
        pixelFormat.setFlags(pixelFormat.getFlags()
            | DDSConstants.DDPF_FOURCC);
        pixelFormat.setFourCC(compressor.getDXTFormat());

        DDSHeader header = new DDSHeader();
        header.setFlags(header.getFlags()
            | DDSConstants.DDSD_WIDTH
            | DDSConstants.DDSD_HEIGHT
            | DDSConstants.DDSD_LINEARSIZE
            | DDSConstants.DDSD_PIXELFORMAT
            | DDSConstants.DDSD_CAPS);
        header.setWidth(image.getWidth());
        header.setHeight(image.getHeight());
        header.setLinearSize(compressor.getCompressedSize(image, attributes));
        header.setPixelFormat(pixelFormat);
        header.setCaps(header.getCaps() | DDSConstants.DDSCAPS_TEXTURE);

        return header;
    }

    /**
     * Documentation on the DDS header format is available at <a href="http://msdn.microsoft.com/en-us/library/bb943982(VS.85).aspx">http://msdn.microsoft.com/en-us/library/bb943982(VS.85).aspx</a>.
     *
     * @param header header structure to write.
     * @param buffer buffer that receives the header structure bytes.
     */
    protected void writeDDSHeader(DDSHeader header, java.nio.ByteBuffer buffer)
    {
        int pos = buffer.position();

        buffer.putInt(header.getSize());            // dwSize
        buffer.putInt(header.getFlags());           // dwFlags
        buffer.putInt(header.getHeight());          // dwHeight
        buffer.putInt(header.getWidth());           // dwWidth
        buffer.putInt(header.getLinearSize());      // dwLinearSize
        buffer.putInt(header.getDepth());           // dwDepth
        buffer.putInt(header.getMipMapCount());     // dwMipMapCount
        buffer.position(buffer.position() + 44);    // dwReserved1[11] (unused)
        this.writeDDSPixelFormat(header.getPixelFormat(), buffer); // ddpf
        buffer.putInt(header.getCaps());            // dwCaps
        buffer.putInt(header.getCaps2());           // dwCaps2
        buffer.putInt(header.getCaps3());           // dwCaps3
        buffer.putInt(header.getCaps4());           // dwCaps4
        buffer.position(buffer.position() + 4);     // dwReserved2 (unused)

        buffer.position(pos + header.getSize());
    }

    /**
     * Documentation on the DDS pixel format is available at <a href="http://msdn.microsoft.com/en-us/library/bb943984(VS.85).aspx">http://msdn.microsoft.com/en-us/library/bb943984(VS.85).aspx</a>.
     *
     * @param pixelFormat pixel format structure to write.
     * @param buffer      buffer that receives the pixel format structure bytes.
     */
    protected void writeDDSPixelFormat(DDSPixelFormat pixelFormat, java.nio.ByteBuffer buffer)
    {
        int pos = buffer.position();

        buffer.putInt(pixelFormat.getSize());           // dwSize
        buffer.putInt(pixelFormat.getFlags());          // dwFlags
        buffer.putInt(pixelFormat.getFourCC());         // dwFourCC
        buffer.putInt(pixelFormat.getRGBBitCount());    // dwRGBBitCount
        buffer.putInt(pixelFormat.getRBitMask());       // dwRBitMask
        buffer.putInt(pixelFormat.getGBitMask());       // dwGBitMask
        buffer.putInt(pixelFormat.getBBitMask());       // dwBBitMask
        buffer.putInt(pixelFormat.getABitMask());       // dwABitMask

        buffer.position(pos + pixelFormat.getSize());
    }
}
