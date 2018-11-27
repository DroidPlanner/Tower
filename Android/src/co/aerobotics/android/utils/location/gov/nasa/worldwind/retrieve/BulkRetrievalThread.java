/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.FileStore;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.event.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Sector;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Retrieves data for a {@link BulkRetrievable}.
 *
 * @author Patrick Murris
 * @version $Id: BulkRetrievalThread.java 1 2011-07-16 23:22:47Z dcollins $
 */
public abstract class BulkRetrievalThread extends Thread
{
    protected int RETRIEVAL_SERVICE_POLL_DELAY = 1000;

    protected final BulkRetrievable retrievable;
    protected final Sector sector;
    protected final double resolution;
    protected final Progress progress;
    protected final FileStore fileStore;
    protected List<BulkRetrievalListener> retrievalListeners = new CopyOnWriteArrayList<BulkRetrievalListener>();

    /**
     * Construct a thread that attempts to download to a specified {@link FileStore} a retrievable's data for a given
     * {@link Sector} and resolution.
     * <p/>
     * This method creates and starts a thread to perform the download. A reference to the thread is returned. To create
     * a downloader that has not been started, construct a {@link gov.nasa.worldwind.terrain.BasicElevationModelBulkDownloader}.
     * <p/>
     * Note that the target resolution must be provided in radians of latitude per texel, which is the resolution in
     * meters divided by the globe radius.
     *
     * @param retrievable the retrievable to retrieve data for.
     * @param sector      the sector of interest.
     * @param resolution  the target resolution, provided in radians of latitude per texel.
     * @param fileStore   the file store to examine.
     * @param listener    an optional retrieval listener. May be null.
     *
     * @throws IllegalArgumentException if either the retrievable, sector or file store are null, or the resolution is
     *                                  less than or equal to zero.
     */
    public BulkRetrievalThread(BulkRetrievable retrievable, Sector sector, double resolution, FileStore fileStore,
        BulkRetrievalListener listener)
    {
        if (retrievable == null)
        {
            String msg = Logging.getMessage("nullValue.RetrievableIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (fileStore == null)
        {
            String msg = Logging.getMessage("nullValue.FileStoreIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
//
//        if (resolution <= 0)
//        {
//            String msg = Logging.getMessage("generic.ResolutionInvalid", resolution);
//            Logging.logger().severe(msg);
//            throw new IllegalArgumentException(msg);
//        }

        this.retrievable = retrievable;
        this.sector = sector;
        this.resolution = resolution;
        this.fileStore = fileStore;
        this.progress = new Progress();

        if (listener != null)
            this.addRetrievalListener(listener);
    }

    public abstract void run();

    /**
     * Get the {@link BulkRetrievable} instance for which this thread acts.
     *
     * @return the {@link BulkRetrievable} instance.
     */
    public BulkRetrievable getRetrievable()
    {
        return this.retrievable;
    }

    /**
     * Get the requested {@link Sector}.
     *
     * @return the requested {@link Sector}.
     */
    public Sector getSector()
    {
        return this.sector;
    }

    /**
     * Get the requested resolution.
     *
     * @return the requested resolution.
     */
    public double getResolution()
    {
        return this.resolution;
    }

    /**
     * Get the file store.
     *
     * @return the file store associated with this downloader.
     */
    public FileStore getFileStore()
    {
        return fileStore;
    }

    /**
     * Get a {@link Progress} instance providing information about this task progress.
     *
     * @return a {@link Progress} instance providing information about this task progress.
     */
    public Progress getProgress()
    {
        return this.progress;
    }

    public void addRetrievalListener(BulkRetrievalListener listener)
    {
        if (listener != null)
            this.retrievalListeners.add(listener);
    }

    public void removeRetrievalListener(BulkRetrievalListener listener)
    {
        if (listener != null)
            this.retrievalListeners.remove(listener);
    }

    protected boolean hasRetrievalListeners()
    {
        return this.retrievalListeners.size() > 0;
    }

    protected void callRetrievalListeners(BulkRetrievalEvent event)
    {
        for (BulkRetrievalListener listener : this.retrievalListeners)
        {
            listener.eventOccurred(event);
        }
    }
}
