/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.View;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.DrawContext;

import java.util.*;

/**
 * Large images and most imagery and elevation-data sets are subdivided in order to display visible portions quickly and
 * without excessive memory usage. Each subdivision is called a tile, and a collections of adjacent tiles corresponding
 * to a common spatial resolution is typically maintained in a {@link Level}. A collection of levels of progressive
 * resolutions are maintained in a {@link LevelSet}. The <code>Tile</code> class represents a single tile of a
 * subdivided image or elevation raster.
 * <p/>
 * Individual tiles are identified by the level, row and column of the tile within its containing level set.
 *
 * @author dcollins
 * @version $Id$
 */
public class Tile implements Cacheable
{
    public interface TileFactory
    {
        Tile createTile(Sector sector, Level level, int row, int column);
    }

    protected Sector sector;
    protected Level level;
    protected int row;
    protected int column;
    /** An optional cache name. Overrides the Level's cache name when non-null. */
    protected String cacheName;
    protected TileKey tileKey;
    protected Vec4[] referencePoints;
    protected double priority = Double.MAX_VALUE; // Default is minimum priority
    // The following is late bound because it's only selectively needed and costly to create
    protected String path;

    /**
     * Constructs a tile for a given sector, level, row and column of the tile's containing tile set.
     *
     * @param sector the sector corresponding with the tile.
     * @param level  the tile's level within a containing level set.
     * @param row    the row index (0 origin) of the tile within the indicated level.
     * @param column the column index (0 origin) of the tile within the indicated level.
     *
     * @throws IllegalArgumentException if <code>sector</code> or <code>level</code> is null.
     */
    public Tile(Sector sector, Level level, int row, int column)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (level == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sector = sector;
        this.level = level;
        this.row = row;
        this.column = column;
        this.cacheName = null;
        this.tileKey = new TileKey(this);
        this.path = null;
    }

    /**
     * Constructs a tile for a given sector, level, row and column of the tile's containing tile set. If the cache name
     * is non-null, it overrides the level's cache name and is returned by {@link #getCacheName()}. Otherwise, the
     * level's cache name is used.
     *
     * @param sector    the sector corresponding with the tile.
     * @param level     the tile's level within a containing level set.
     * @param row       the row index (0 origin) of the tile within the indicated level.
     * @param column    the column index (0 origin) of the tile within the indicated level.
     * @param cacheName optional cache name to override the Level's cache name. May be null.
     *
     * @throws IllegalArgumentException if <code>sector</code> or <code>level</code> is null.
     */
    public Tile(Sector sector, Level level, int row, int column, String cacheName)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (level == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sector = sector;
        this.level = level;
        this.row = row;
        this.column = column;
        this.cacheName = cacheName;
        this.tileKey = new TileKey(this);
        this.path = null;
    }

    /**
     * Constructs a texture tile for a given sector and level, and with a default row and column.
     *
     * @param sector the sector to create the tile for.
     * @param level  the level to associate the tile with
     *
     * @throws IllegalArgumentException if sector or level are null.
     */
    public Tile(Sector sector, Level level)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (level == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sector = sector;
        this.level = level;
        this.row = Tile.computeRow(sector.getDeltaLat(), sector.minLatitude, Angle.fromDegrees(-90));
        this.column = Tile.computeColumn(sector.getDeltaLon(), sector.minLongitude, Angle.fromDegrees(-180));
        this.cacheName = null;
        this.tileKey = new TileKey(this);
        this.path = null;
    }

    /**
     * Constructs a texture tile for a given sector with a default level, row and column.
     *
     * @param sector the sector to create the tile for.
     */
    public Tile(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Random random = new Random();

        this.sector = sector;
        this.level = null;
        this.row = random.nextInt();
        this.column = random.nextInt();
        this.cacheName = null;
        this.tileKey = new TileKey(this);
        this.path = null;
    }

    public long getSizeInBytes()
    {
        // Return just an approximate size
        long size = 0;

        if (this.sector != null)
            size += this.sector.getSizeInBytes();

        if (this.path != null)
            size += this.getPath().length();

        size += 32; // to account for the references and the TileKey size

        return size;
    }

    public String getPath()
    {
        if (this.path == null)
        {
            this.path = this.level.getPath() + "/" + this.row + "/" + this.row + "_" + this.column;
            if (!this.level.isEmpty())
                path += this.level.getFormatSuffix();
        }

        return this.path;
    }

    public String getPathBase()
    {
        String path = this.getPath();

        return path.contains(".") ? path.substring(0, path.lastIndexOf(".")) : path;
    }

    public final Sector getSector()
    {
        return sector;
    }

    public Level getLevel()
    {
        return level;
    }

    public final int getLevelNumber()
    {
        return this.level != null ? this.level.getLevelNumber() : 0;
    }

    public final String getLevelName()
    {
        return this.level != null ? this.level.getLevelName() : "";
    }

    public final int getRow()
    {
        return row;
    }

    public final int getColumn()
    {
        return column;
    }

    /**
     * Returns the tile's cache name. If a non-null cache name was specified at construction, that name is returned.
     * Otherwise this returns the level's cache name.
     *
     * @return the tile's cache name.
     */
    public final String getCacheName()
    {
        if (this.cacheName != null)
            return this.cacheName;

        return this.level != null ? this.level.getCacheName() : null;
    }

    public final String getFormatSuffix()
    {
        return this.level != null ? this.level.getFormatSuffix() : null;
    }

    public final TileKey getTileKey()
    {
        return this.tileKey;
    }

    public java.net.URL getResourceURL() throws java.net.MalformedURLException
    {
        return this.level != null ? this.level.getTileResourceURL(this, null) : null;
    }

    public java.net.URL getResourceURL(String imageFormat) throws java.net.MalformedURLException
    {
        return this.level != null ? this.level.getTileResourceURL(this, imageFormat) : null;
    }

    public String getLabel()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(this.getLevelNumber());
        sb.append("(");
        sb.append(this.getLevelName());
        sb.append(")");
        sb.append(", ").append(this.getRow());
        sb.append(", ").append(this.getColumn());

        return sb.toString();
    }

    public int getWidth()
    {
        return this.getLevel().getTileWidth();
    }

    public int getHeight()
    {
        return this.getLevel().getTileHeight();
    }

    public int compareTo(Tile tile)
    {
        if (tile == null)
        {
            String msg = Logging.getMessage("nullValue.TileIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // No need to compare Sectors or path because they are redundant with row and column
        if (tile.getLevelNumber() == this.getLevelNumber() && tile.row == this.row && tile.column == this.column)
            return 0;

        if (this.getLevelNumber() < tile.getLevelNumber()) // Lower-res levels compare lower than higher-res
            return -1;
        if (this.getLevelNumber() > tile.getLevelNumber())
            return 1;

        if (this.row < tile.row)
            return -1;
        if (this.row > tile.row)
            return 1;

        if (this.column < tile.column)
            return -1;

        return 1; // tile.column must be > this.column because equality was tested above
    }

    @Override
    public boolean equals(Object o)
    {
        // Equality based only on the tile key
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final Tile tile = (Tile) o;

        return !(tileKey != null ? !tileKey.equals(tile.tileKey) : tile.tileKey != null);
    }

    @Override
    public int hashCode()
    {
        return (tileKey != null ? tileKey.hashCode() : 0);
    }

    @Override
    public String toString()
    {
        return this.getPath();
    }

    public Vec4[] getReferencePoints()
    {
        return this.referencePoints;
    }

    public void setReferencePoints(Vec4[] points)
    {
        this.referencePoints = points;
    }

    public boolean mustSubdivide(DrawContext dc, double detailFactor)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4[] points = this.referencePoints;
        if (points == null)
        {
            points = new Vec4[] {new Vec4(), new Vec4(), new Vec4(), new Vec4(), new Vec4()};
            this.sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration(), points);
            this.sector.computeCentroidPoint(dc.getGlobe(), dc.getVerticalExaggeration(), points[4]);
        }

        // Get the eye distance squared for each of the sector's corners and its center. We use the distance squared
        // because we're only concerned about magnitude here, and using distance squared enables us to reduce the number
        // of sqrt calls from 5 to 1.
        View view = dc.getView();
        double d1 = view.getEyePoint().distanceToSquared3(points[0]);
        double d2 = view.getEyePoint().distanceToSquared3(points[1]);
        double d3 = view.getEyePoint().distanceToSquared3(points[2]);
        double d4 = view.getEyePoint().distanceToSquared3(points[3]);
        double d5 = view.getEyePoint().distanceToSquared3(points[4]);

        // Find the minimum eye distance squared. Compute cell height at the corresponding point. Cell height is
        // radius * radian texel size.
        double minDistanceSq = d1;
        double cellHeight = points[0].getLength3() * this.level.getTexelSize();
        double texelSize = level.getTexelSize();

        if (d2 < minDistanceSq)
        {
            minDistanceSq = d2;
            cellHeight = points[1].getLength3() * texelSize;
        }
        if (d3 < minDistanceSq)
        {
            minDistanceSq = d3;
            cellHeight = points[2].getLength3() * texelSize;
        }
        if (d4 < minDistanceSq)
        {
            minDistanceSq = d4;
            cellHeight = points[3].getLength3() * texelSize;
        }
        if (d5 < minDistanceSq)
        {
            minDistanceSq = d5;
            cellHeight = points[4].getLength3() * texelSize;
        }

        // Split when the cell height (length of a texel) becomes greater than the specified fraction of the eye
        // distance. The fraction is specified as a power of 10. For example, a detail factor of 3 means split when the
        // cell height becomes more than one thousandth of the eye distance. Another way to say it is, use the current
        // tile if its cell height is less than the specified fraction of the eye distance.
        //
        // NOTE: It's tempting to instead compare a screen pixel size to the texel size, but that calculation is
        // window-size dependent and results in selecting an excessive number of tiles when the window is large.
        return cellHeight > Math.sqrt(minDistanceSq) * Math.pow(10, -detailFactor);
    }

    /**
     * Splits this tile into four tiles; one for each sub quadrant of this tile. This attempts to retrieve each sub tile
     * from the tile cache.
     *
     * @param nextLevel the level for the sub tiles.
     *
     * @return a four-element array that contains this tile's sub tiles.
     *
     * @throws IllegalArgumentException if <code>nextLevel</code> is <code>null</code>.
     */
    public Tile[] subdivide(Level nextLevel, MemoryCache cache, TileFactory factory)
    {
        if (nextLevel == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (cache == null)
        {
            String msg = Logging.getMessage("nullValue.CacheIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (factory == null)
        {
            String msg = Logging.getMessage("nullValue.FactoryIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Tile[] result = new Tile[4];

        double p0 = this.sector.minLatitude.degrees;
        double p2 = this.sector.maxLatitude.degrees;
        double p1 = 0.5 * (p0 + p2);

        double t0 = this.sector.minLongitude.degrees;
        double t2 = this.sector.maxLongitude.degrees;
        double t1 = 0.5 * (t0 + t2);

        int subRow = 2 * this.row;
        int subCol = 2 * this.column;
        TileKey newTileKey = new TileKey(nextLevel.getLevelNumber(), subRow, subCol, nextLevel.getCacheName());
        Tile subTile = (Tile) cache.get(newTileKey);
        if (subTile != null)
            result[0] = subTile;
        else
            result[0] = factory.createTile(Sector.fromDegrees(p0, p1, t0, t1), nextLevel, subRow, subCol);

        subRow = 2 * this.row;
        subCol = 2 * this.column + 1;
        newTileKey = new TileKey(nextLevel.getLevelNumber(), subRow, subCol, nextLevel.getCacheName());
        subTile = (Tile) cache.get(newTileKey);
        if (subTile != null)
            result[1] = subTile;
        else
            result[1] = factory.createTile(Sector.fromDegrees(p0, p1, t1, t2), nextLevel, subRow, subCol);

        subRow = 2 * this.row + 1;
        subCol = 2 * this.column;
        newTileKey = new TileKey(nextLevel.getLevelNumber(), subRow, subCol, nextLevel.getCacheName());
        subTile = (Tile) cache.get(newTileKey);
        if (subTile != null)
            result[2] = subTile;
        else
            result[2] = factory.createTile(Sector.fromDegrees(p1, p2, t0, t1), nextLevel, subRow, subCol);

        subRow = 2 * this.row + 1;
        subCol = 2 * this.column + 1;
        newTileKey = new TileKey(nextLevel.getLevelNumber(), subRow, subCol, nextLevel.getCacheName());
        subTile = (Tile) cache.get(newTileKey);
        if (subTile != null)
            result[3] = subTile;
        else
            result[3] = factory.createTile(Sector.fromDegrees(p1, p2, t1, t2), nextLevel, subRow, subCol);

        return result;
    }

    public static void createTilesForLevel(Level level, Sector sector, TileFactory factory, List<Tile> result)
    {
        if (level == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (factory == null)
        {
            String msg = Logging.getMessage("nullValue.FactoryIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle deltaLat = level.tileDelta.latitude;
        Angle deltaLon = level.tileDelta.longitude;

        int firstRow = computeRow(deltaLat, sector.minLatitude, Angle.fromDegrees(-90));
        int lastRow = computeRow(deltaLat, sector.maxLatitude, Angle.fromDegrees(-90));
        int firstCol = computeColumn(deltaLon, sector.minLongitude, Angle.fromDegrees(-180));
        int lastCol = computeColumn(deltaLon, sector.maxLongitude, Angle.fromDegrees(-180));

        Angle firstRowLat = computeRowLatitude(firstRow, deltaLat, Angle.fromDegrees(-90));
        Angle firstRowLon = computeColumnLongitude(firstCol, deltaLon, Angle.fromDegrees(-180));

        double minLat = firstRowLat.degrees;
        double minLon;
        double maxLat;
        double maxLon;

        for (int row = firstRow; row <= lastRow; row++)
        {
            maxLat = minLat + deltaLat.degrees;
            minLon = firstRowLon.degrees;

            for (int col = firstCol; col <= lastCol; col++)
            {
                maxLon = minLon + deltaLon.degrees;

                result.add(factory.createTile(Sector.fromDegrees(minLat, maxLat, minLon, maxLon), level, row, col));

                minLon = maxLon;
            }

            minLat = maxLat;
        }
    }

    /**
     * Computes the row index of a latitude in the global tile grid corresponding to a specified grid interval.
     *
     * @param delta    the grid interval
     * @param latitude the latitude for which to compute the row index
     * @param origin   the origin of the grid
     *
     * @return the row index of the row containing the specified latitude
     *
     * @throws IllegalArgumentException if <code>delta</code> is null or non-positive, or <code>latitude</code> is null,
     *                                  greater than positive 90 degrees, or less than  negative 90 degrees
     */
    public static int computeRow(Angle delta, Angle latitude, Angle origin)
    {
        if (delta == null || latitude == null || origin == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (latitude.degrees < -90d || latitude.degrees > 90d)
        {
            String message = Logging.getMessage("generic.AngleOutOfRange", latitude);
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        int row = (int) ((latitude.degrees - origin.degrees) / delta.degrees);
        // Latitude is at the end of the grid. Subtract 1 from the computed row to return the last row.
        if ((latitude.degrees - origin.degrees) == 180d)
            row = row - 1;

        return row;
    }

    /**
     * Computes the column index of a longitude in the global tile grid corresponding to a specified grid interval.
     *
     * @param delta     the grid interval
     * @param longitude the longitude for which to compute the column index
     * @param origin    the origin of the grid
     *
     * @return the column index of the column containing the specified latitude
     *
     * @throws IllegalArgumentException if <code>delta</code> is null or non-positive, or <code>longitude</code> is
     *                                  null, greater than positive 180 degrees, or less than  negative 180 degrees
     */
    public static int computeColumn(Angle delta, Angle longitude, Angle origin)
    {
        if (delta == null || longitude == null || origin == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (longitude.degrees < -180d || longitude.degrees > 180d)
        {
            String message = Logging.getMessage("generic.AngleOutOfRange", longitude);
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        // Compute the longitude relative to the grid. The grid provides 360 degrees of longitude from the grid origin.
        // We wrap grid longitude values so that the grid begins and ends at the origin.
        double gridLongitude = longitude.degrees - origin.degrees;
        if (gridLongitude < 0.0)
            gridLongitude = 360d + gridLongitude;

        int col = (int) (gridLongitude / delta.degrees);
        // Longitude is at the end of the grid. Subtract 1 from the computed column to return the last column.
        if ((longitude.degrees - origin.degrees) == 360d)
            col = col - 1;

        return col;
    }

    /**
     * Determines the minimum latitude of a row in the global tile grid corresponding to a specified grid interval.
     *
     * @param row    the row index of the row in question
     * @param delta  the grid interval
     * @param origin the origin of the grid
     *
     * @return the minimum latitude of the tile corresponding to the specified row
     *
     * @throws IllegalArgumentException if the grid interval (<code>delta</code>) is null or zero or the row index is
     *                                  negative.
     */
    public static Angle computeRowLatitude(int row, Angle delta, Angle origin)
    {
        if (delta == null || origin == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (row < 0)
        {
            String msg = Logging.getMessage("generic.RowIndexOutOfRange", row);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        double latDegrees = origin.degrees + (row * delta.degrees);
        return Angle.fromDegrees(latDegrees);
    }

    /**
     * Determines the minimum longitude of a column in the global tile grid corresponding to a specified grid interval.
     *
     * @param column the row index of the row in question
     * @param delta  the grid interval
     * @param origin the origin of the grid
     *
     * @return the minimum longitude of the tile corresponding to the specified column
     *
     * @throws IllegalArgumentException if the grid interval (<code>delta</code>) is null or zero or the column index is
     *                                  negative.
     */
    public static Angle computeColumnLongitude(int column, Angle delta, Angle origin)
    {
        if (delta == null || origin == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
        if (column < 0)
        {
            String msg = Logging.getMessage("generic.ColumnIndexOutOfRange", column);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        double lonDegrees = origin.degrees + (column * delta.degrees);
        return Angle.fromDegrees(lonDegrees);
    }

    public double getPriority()
    {
        return this.priority;
    }

    public void setPriority(double priority)
    {
        this.priority = priority;
    }
}
