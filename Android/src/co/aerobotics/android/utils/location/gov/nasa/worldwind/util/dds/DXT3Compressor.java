/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.util.dds;

import android.graphics.Bitmap;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class DXT3Compressor implements DXTCompressor
{
    public DXT3Compressor()
    {
    }

    public int getDXTFormat()
    {
        return DDSConstants.D3DFMT_DXT3;
    }

    public int getCompressedSize(Bitmap image, DXTCompressionAttributes attributes)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: comment, provide documentation reference

        int width = Math.max(image.getWidth(), 4);
        int height = Math.max(image.getHeight(), 4);
        
        return (width * height);
    }
    
    public void compressImage(Bitmap image, DXTCompressionAttributes attributes,
        java.nio.ByteBuffer buffer)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        ColorBlock4x4 colorBlock = new ColorBlock4x4();
        ColorBlockExtractor colorBlockExtractor = this.getColorBlockExtractor(image);

        BlockDXT3 dxt3Block = new BlockDXT3();
        BlockDXT3Compressor dxt3Compressor = new BlockDXT3Compressor();

        int width = image.getWidth();
        int height = image.getHeight();

        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                colorBlockExtractor.extractColorBlock4x4(attributes, i, j, colorBlock);
                dxt3Compressor.compressBlockDXT3(colorBlock, attributes, dxt3Block);

                AlphaBlockDXT3 dxtAlphaBlock = dxt3Block.getAlphaBlock();
                buffer.putLong(dxtAlphaBlock.alphaValueMask);

                BlockDXT1 dxtColorBlock = dxt3Block.getColorBlock();
                buffer.putShort((short) dxtColorBlock.color0);
                buffer.putShort((short) dxtColorBlock.color1);
                buffer.putInt((int) dxtColorBlock.colorIndexMask);
            }
        }
    }

    protected ColorBlockExtractor getColorBlockExtractor(Bitmap image)
    {
        return new BasicColorBlockExtractor(image);
    }
}
