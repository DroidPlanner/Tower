/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Angle;

/**
 * @author dcollins
 * @version $Id$
 */
public class TileKey implements Comparable<TileKey>
{
    protected int level;
    protected int row;
    protected int col;
    protected String cacheName;
    protected int hash;

    /**
     * @param level
     * @param row
     * @param col
     * @param cacheName
     * @throws IllegalArgumentException if <code>level</code>, <code>row</code> or <code>column</code> is negative or if
     *                                  <code>cacheName</code> is null or empty
     */
    public TileKey(int level, int row, int col, String cacheName)
    {
        if (level < 0)
        {
            String msg = Logging.getMessage("TileKey.levelIsLessThanZero");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (cacheName == null || cacheName.length() < 1)
        {
            String msg = Logging.getMessage("TileKey.cacheNameIsNullOrEmpty");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.level = level;
        this.row = row;
        this.col = col;
        this.cacheName = cacheName;
        this.hash = this.computeHash();
    }

    /**
     * @param latitude
     * @param longitude
     * @param levelNumber
     * @throws IllegalArgumentException if any parameter is null
     */
    public TileKey(Angle latitude, Angle longitude, LevelSet levelSet, int levelNumber)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (levelSet == null)
        {
            String msg = Logging.getMessage("nullValue.LevelSetIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        Level l = levelSet.getLevel(levelNumber);
        this.level = levelNumber;
        this.row = Tile.computeRow(l.getTileDelta().latitude, latitude, levelSet.getTileOrigin().latitude);
        this.col = Tile.computeColumn(l.getTileDelta().longitude, longitude, levelSet.getTileOrigin().longitude);
        this.cacheName = l.getCacheName();
        this.hash = this.computeHash();
    }

    /**
     * @param tile
     * @throws IllegalArgumentException if <code>tile</code> is null
     */
    public TileKey(Tile tile)
    {
        if (tile == null)
        {
            String msg = Logging.getMessage("nullValue.TileIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        this.level = tile.getLevelNumber();
        this.row = tile.getRow();
        this.col = tile.getColumn();
        this.cacheName = tile.getCacheName();
        this.hash = this.computeHash();
    }

    public int getLevelNumber()
    {
        return level;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return col;
    }

    public String getCacheName()
    {
        return cacheName;
    }

    protected int computeHash()
    {
        int result;
        result = this.level;
        result = 29 * result + this.row;
        result = 29 * result + this.col;
        result = 29 * result + (this.cacheName != null ? this.cacheName.hashCode() : 0);
        return result;
    }

    /**
     * Compare two tile keys. Keys are ordered based on level, row, and column (in that order).
     *
     * @param key Key to compare with.
     *
     * @return 0 if the keys are equal. 1 if this key &gt; {@code key}. -1 if this key &lt; {@code key}.
     *
     * @throws IllegalArgumentException if <code>key</code> is null
     */
    public final int compareTo(TileKey key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // No need to compare Sectors because they are redundant with row and column
        if (key.level == this.level && key.row == this.row && key.col == this.col)
            return 0;

        if (this.level < key.level) // Lower-res levels compare lower than higher-res
            return -1;
        if (this.level > key.level)
            return 1;

        if (this.row < key.row)
            return -1;
        if (this.row > key.row)
            return 1;

        if (this.col < key.col)
            return -1;

        return 1; // tile.col must be > this.col because equality was tested above
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final TileKey tileKey = (TileKey) o;

        if (this.col != tileKey.col)
            return false;
        if (this.level != tileKey.level)
            return false;
        //noinspection SimplifiableIfStatement
        if (this.row != tileKey.row)
            return false;

        return !(this.cacheName != null ? !this.cacheName.equals(tileKey.cacheName) : tileKey.cacheName != null);
    }

    @Override
    public int hashCode()
    {
        return this.hash;
    }

    @Override
    public String toString()
    {
        return this.cacheName + "/" + this.level + "/" + this.row + "/" + col;
    }
}
