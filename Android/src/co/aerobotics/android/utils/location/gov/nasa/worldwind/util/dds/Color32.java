/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.util.dds;

/**
 * 32 bit 8888 ARGB color.
 *
 * @author dcollins
 * @version $Id$
 */
public class Color32 extends Color24
{
    /**
     * The alpha component.
     */
    public int a;

    /**
     * Creates a 32 bit 8888 ARGB color with all values set to 0.
     */
    public Color32()
    {
        super();
        this.a = 0;
    }

    public Color32(int a, int r, int g, int b)
    {
        super(r, g, b);
        this.a = a;
    }

    public static Color32 multiplyAlpha(Color32 color)
    {
        if (null == color)
        {
            return null;
        }

        Color32 result = new Color32();

        double alphaF = color.a / 256d;

        result.a = color.a;
        result.r = (int) (color.r * alphaF);
        result.g = (int) (color.g * alphaF);
        result.b = (int) (color.b * alphaF);

        return result;
    }
}
