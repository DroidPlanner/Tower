/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

/**
 * @author Tom Gaskins
 * @version $Id$
 */
public interface FileStore
{
    /**
     * Returns the locations that the file store will look for files.
     *
     * @return the list of locations the file store will search when a file is requested.
     */
    java.util.List<? extends java.io.File> getLocations();

    /**
     * Returns the location that additions to the file store are placed.
     *
     * @return the location at which new entries are placed.
     */
    java.io.File getWriteLocation();

    /**
     * Adds a location to search when files are requested from the file store.
     *
     * @param newPath   the location to add. If the location already exists in the list of read locations its entry is
     *                  removed and a new entry is added to the end of the search list.
     * @param isInstall indicates whether the location is an "installed data" location and therefore not subject to
     *                  automatic removal of its contents.
     *
     * @throws IllegalArgumentException if the specified path is null or empty.
     */
    void addLocation(String newPath, boolean isInstall);

    /**
     * Adds a location to search when files are requested from the file store and specifies its location in the search
     * order.
     *
     * @param index     the location in the search list at which to add the new location.
     * @param newPath   the location to add.
     * @param isInstall indicates whether the location is an installed-data location and therefore not subject to
     *                  automatic removal of its contents.
     *
     * @throws IllegalArgumentException if the specified path is null or empty or the specified index is less than 0.
     */
    void addLocation(int index, String newPath, boolean isInstall);

    /**
     * Remove a specified read location from the file store. The current write location cannot be removed.
     *
     * @param path the read location to remove.
     *
     * @throws IllegalArgumentException if the specified path is null or identifies the current write location.
     */
    void removeLocation(String path);

    /**
     * Indicates whether the file store contains a specified file.
     *
     * @param fileName the file in question.
     *
     * @return true if the file store contains the file, false if the file store does not contain the file or the
     *         specified path is null.
     */
    boolean containsFile(String fileName);

    /**
     * Searches the file store for a specified file and returns a reference to it if it is.
     *
     * @param fileName       the file to search for, identified by a path relative to the root of the file store.
     * @param checkClassPath if true, the current classpath is first searched for the file, otherwise the classpath is
     *                       not searched.
     *
     * @return a URL addressing the file if it is found.
     *
     * @throws IllegalArgumentException if the specified path is null.
     */
    java.net.URL findFile(String fileName, boolean checkClassPath);

    /**
     * Creates a new, empty file in the file store.
     * <p/>
     * If the file store has no write location, the file is not created and null is returned.
     *
     * @param fileName the name of the file to create.
     *
     * @return a reference to the newly created file.
     *
     * @throws IllegalArgumentException if the specified path is null.
     */
    java.io.File newFile(String fileName);

    /**
     * Removes a file from the file store using the URL to the cached file.
     *
     * @param url a URL, as returned by {@link #findFile(String, boolean)} identifying the file.
     *
     * @throws IllegalArgumentException if the specified URL is null.
     */
    void removeFile(java.net.URL url);

    /**
     * Returns an array of strings naming the files discovered directly under a specified file store path name. If the
     * path name is null, files under the store root are searched. This returns null if the path does not exist in the
     * store. Returned names are relative pointers to a file in the store; they are not necessarily a file system path.
     *
     * @param pathName relative path in the file store to search, or null to search the entire file store.
     * @param filter   a file filter.
     *
     * @return an array of file store names. Returns null if the path does not exist in the file store.
     *
     * @throws IllegalArgumentException if the filter is null.
     */
    String[] listFileNames(String pathName, FileStoreFilter filter);

    /**
     * Returns an array of strings naming the files discovered under a specified file store path name. If the path name
     * is null, the entire file store will be searched. Otherwise the file store is recursively searched under the
     * specified path name for files accepted by the specified filter, until the entire path tree is exhausted. This
     * returns null if the path does not exist in the store. Returned names are relative pointers to a file in the
     * store; they are not necessarily a file system path.
     *
     * @param pathName relative path in the file store to search, or null to search the entire file store.
     * @param filter   a file filter.
     *
     * @return an array of file store names. Returns null if the path does not exist in the file store.
     *
     * @throws IllegalArgumentException if the filter is null.
     */
    String[] listAllFileNames(String pathName, FileStoreFilter filter);

    /**
     * Returns an array of strings naming the files discovered under a specified file store path name. If the path name
     * is null, the entire file store will be searched. Otherwise the file store is recursively searched under each
     * branch of the the specified path name until a matching file is found, or that branch is exhausted. Unlike {@link
     * #listAllFileNames(String, FileStoreFilter)}, This has the effect of locating the top file name under each branch.
     * This returns null if the path does not exist in the store. Returned names are relative pointers to a file in the
     * store; they are not necessarily a file system path.
     *
     * @param pathName relative path in the file store to search, or null to search the entire file store.
     * @param filter   a file filter.
     *
     * @return an array of file store names. Returns null if the path does not exist in the file store.
     *
     * @throws IllegalArgumentException if the filter is null.
     */
    String[] listTopFileNames(String pathName, FileStoreFilter filter);
}
