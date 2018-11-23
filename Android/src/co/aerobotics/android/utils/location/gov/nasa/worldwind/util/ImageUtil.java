/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import android.graphics.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Sector;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

/** @version $Id$ */
public class ImageUtil
{
    /**
     * Draws the specified <code>image</code> onto the <code>canvas</code>, scaling or stretching the image to fit the
     * canvas. This will apply a bilinear filter to the image if any scaling or stretching is necessary.
     *
     * @param image  the Bitmap to draw, potentially scaling or stretching to fit the <code>canvas</code>.
     * @param canvas the Bitmap to receive the scaled or stretched <code>image</code>.
     *
     * @throws IllegalArgumentException if either <code>image</code> or <code>canvas</code> is null.
     */
    public static void getScaledCopy(Bitmap image, Bitmap canvas)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.CanvasIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Canvas graphicsCanvas = new Canvas(canvas);
        graphicsCanvas.drawBitmap(image, 0, 0, null);
    }

    /**
     * Merge an image into another image. This method is typically used to assemble a composite, seamless image from
     * several individual images. The receiving image, called here the canvas because it's analogous to the Photoshop
     * notion of a canvas, merges the incoming image according to the specified aspect ratio.
     *
     * @param canvasSector the sector defining the canvas' location and range.
     * @param imageSector  the sector defining the image's location and range.
     * @param aspectRatio  the aspect ratio, width/height, of the assembled image. If the aspect ratio is greater than
     *                     or equal to one, the assembled image uses the full width of the canvas; the height used is
     *                     proportional to the inverse of the aspect ratio. If the aspect ratio is less than one, the
     *                     full height of the canvas is used; the width used is proportional to the aspect ratio. <p/>
     *                     The aspect ratio is typically used to maintain consistent width and height units while
     *                     assembling multiple images into a canvas of a different aspect ratio than the canvas sector,
     *                     such as drawing a non-square region into a 1024x1024 canvas. An aspect ratio of 1 causes the
     *                     incoming images to be stretched as necessary in one dimension to match the aspect ratio of
     *                     the canvas sector.
     * @param image        the image to merge into the canvas.
     * @param canvas       the canvas into which the images are merged. The canvas is not changed if the specified image
     *                     and canvas sectors are disjoint.
     *
     * @throws IllegalArgumentException if the any of the reference arguments are null or the aspect ratio is less than
     *                                  or equal to zero.
     */
    public static void mergeImage(Sector canvasSector, Sector imageSector, double aspectRatio, Bitmap image,
        Bitmap canvas)
    {
        if (canvasSector == null || imageSector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        if (canvas == null || image == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        if (aspectRatio <= 0)
        {
            String message = Logging.getMessage("Util.AspectRatioInvalid", aspectRatio);
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        if (!(canvasSector.intersects(imageSector)))
            return;

        // Create an image with the desired aspect ratio within an enclosing canvas of possibly different aspect ratio.
        int subWidth = aspectRatio >= 1 ? canvas.getWidth() : (int) Math.ceil((canvas.getWidth() * aspectRatio));
        int subHeight = aspectRatio >= 1 ? (int) Math.ceil((canvas.getHeight() / aspectRatio)) : canvas.getHeight();

        // yShift shifts image down to change origin from upper-left to lower-left
        float yShift = (float) (aspectRatio >= 1d ? (1d - 1d / aspectRatio) * canvas.getHeight() : 0d);

        float sh = (float) (((double) subHeight / (double) image.getHeight())
            * (imageSector.getDeltaLatDegrees() / canvasSector.getDeltaLatDegrees()));
        float sw = (float) (((double) subWidth / (double) image.getWidth())
            * (imageSector.getDeltaLonDegrees() / canvasSector.getDeltaLonDegrees()));

        float dh = (float) (subHeight *
            (-imageSector.maxLatitude.subtract(canvasSector.maxLatitude).degrees
                / canvasSector.getDeltaLat().degrees));
        float dw = (float) (subWidth *
            (imageSector.minLongitude.subtract(canvasSector.minLongitude).degrees
                / canvasSector.getDeltaLon().degrees));

        Canvas c = new Canvas(canvas);
        c.translate(dw, dh + yShift);
        c.scale(sw, sh);
        c.drawBitmap(image, 0, 0, null);
    }

    public static Bitmap mapTransparencyColors(ByteBuffer imageBuffer, int originalColors[])
    {
        InputStream inputStream = WWIO.getInputStreamFromByteBuffer(imageBuffer);
        Bitmap image = BitmapFactory.decodeStream(inputStream);
        if (image == null)
        {
            return null;
        }
        return mapTransparencyColors(image, originalColors);
    }

    public static Bitmap mapTransparencyColors(Bitmap sourceImage, int[] originalColors)
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        if (originalColors == null)
        {
            String message = Logging.getMessage("nullValue.ColorArrayIsNull");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        if (width < 1 || height < 1)
        {
            String message = Logging.getMessage("ImageUtil.EmptyImage");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        int[] sourceColors = new int[width * height];
        sourceImage.getPixels(sourceColors, 0, width, 0, 0, width, height);
        int[] destColors = Arrays.copyOf(sourceColors, sourceColors.length);

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int index = j * width + i;
                for (int c : originalColors)
                {
                    if (sourceColors[index] == c)
                    {
                        destColors[index] = 0;
                        break;
                    }
                }
            }
        }

        // Release memory used by source colors prior to creating the new image
        //noinspection UnusedAssignment
        sourceColors = null;

        Bitmap destImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        destImage.setPixels(destColors, 0, width, 0, 0, width, height);

        return destImage;
    }

    /**
     * Returns the maximum desired mip level for an image with dimensions <code>width</code> and <code>height</code>.
     * The maximum desired level is the number of levels required to reduce the original image dimensions to a 1x1
     * image.
     *
     * @param width  the level 0 image width.
     * @param height the level 0 image height.
     *
     * @return maximum mip level for the specified <code>width</code> and <code>height</code>.
     *
     * @throws IllegalArgumentException if either <code>width</code> or <code>height</code> are less than 1.
     */
    public static int getMaxMipmapLevel(int width, int height)
    {
        if (width < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 1");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (height < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "height < 1");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        int widthLevels = (int) WWMath.logBase2(width);
        int heightLevels = (int) WWMath.logBase2(height);
        return Math.max(widthLevels, heightLevels);
    }

    /**
     * Builds a sequence of mipmaps for the specified image. The number of mipmap levels created will be equal to
     * <code>maxLevel + 1</code>, including level 0. The level 0 image will be a reference to the original image, not a
     * copy. Each mipmap level will be created with the specified Bitmap type <code>mipmapImageType</code>. Each level
     * will have dimensions equal to 1/2 the previous level's dimensions, rounding down, to a minimum width or height of
     * 1.
     *
     * @param image           the Bitmap to build mipmaps for.
     * @param mipmapImageType the Bitmap type to use when creating each mipmap image.
     * @param maxLevel        the maximum mip level to create. Specifying zero will return an array containing the
     *                        original image.
     *
     * @return array of mipmap levels, starting at level 0 and stopping at maxLevel. This array will have length
     *         maxLevel + 1.
     *
     * @throws IllegalArgumentException if <code>image</code> is null, or if <code>maxLevel</code> is less than zero.
     * @see #getMaxMipmapLevel(int, int)
     */
    public static Bitmap[] buildMipmaps(Bitmap image, Bitmap.Config mipmapImageType, int maxLevel)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (maxLevel < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "maxLevel < 0");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Bitmap[] mipMapLevels = new Bitmap[1 + maxLevel];

        // If the image and mipmap type are equivalent, then just pass the original image along. Otherwise, create a
        // copy of the original image with the appropriate image type.
        if (image.getConfig() == mipmapImageType)
        {
            mipMapLevels[0] = image;
        }
        else
        {
            mipMapLevels[0] = Bitmap.createBitmap(image.getWidth(), image.getHeight(), mipmapImageType);
            getScaledCopy(image, mipMapLevels[0]);
        }

        for (int level = 1; level <= maxLevel; level++)
        {
            int width = Math.max(image.getWidth() >> level, 1);
            int height = Math.max(image.getHeight() >> level, 1);

            mipMapLevels[level] = Bitmap.createBitmap(width, height, mipmapImageType);
            getScaledCopy(mipMapLevels[level - 1], mipMapLevels[level]);
        }

        return mipMapLevels;
    }
}
