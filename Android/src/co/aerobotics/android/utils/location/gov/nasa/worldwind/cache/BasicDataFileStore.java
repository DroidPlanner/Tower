/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.cache;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.Configuration;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @version $Id $
 */
public class BasicDataFileStore implements FileStore
{
    protected static class StoreLocation extends AVListImpl
    {
        protected boolean markWhenUsed = false;

        public StoreLocation(java.io.File file, boolean isInstall)
        {
            this.setValue(AVKey.FILE_STORE_LOCATION, file);
            this.setValue(AVKey.INSTALLED, isInstall);
        }

        public StoreLocation(java.io.File file)
        {
            this(file, false);
        }

        public File getFile()
        {
            Object o = this.getValue(AVKey.FILE_STORE_LOCATION);
            return (o instanceof File) ? (File) o : null;
        }

        public void setFile(java.io.File file)
        {
            this.setValue(AVKey.FILE_STORE_LOCATION, file);
        }

        public boolean isInstall()
        {
            Object o = this.getValue(AVKey.INSTALLED);
            return (o != null && o instanceof Boolean) ? (Boolean) o : false;
        }

        public void setInstall(boolean isInstall)
        {
            this.setValue(AVKey.INSTALLED, isInstall);
        }

        public boolean isMarkWhenUsed()
        {
            return markWhenUsed;
        }

        public void setMarkWhenUsed(boolean markWhenUsed)
        {
            this.markWhenUsed = markWhenUsed;
        }
    }

    // Retrieval could be occurring on several threads when the app adds a read location, so protect the list of read
    // locations from concurrent modification.
    protected List<StoreLocation> readLocations = new CopyOnWriteArrayList<StoreLocation>();
    protected StoreLocation writeLocation = null;
    private final Object fileLock = new Object();

    //**************************************************************//
    //********************  File Store Configuration  **************//
    //**************************************************************//

    /**
     * Create an instance.
     *
     * @throws IllegalStateException if the configuration file name cannot be determined from {@link Configuration} or
     *                               the configuration file cannot be found.
     */
    public BasicDataFileStore()
    {
        String configPath = Configuration.getStringValue(AVKey.DATA_FILE_STORE_CONFIGURATION_FILE_NAME);
        if (configPath == null)
        {
            String message = Logging.getMessage("FileStore.NoConfiguration");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        InputStream is = WWIO.openFileOrResourceStream(configPath, this.getClass());
        if (is == null)
        {
            String message = Logging.getMessage("FileStore.ConfigurationNotFound", configPath);
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        this.initialize(is);
    }

    protected void initialize(InputStream xmlConfigStream)
    {
        Document doc = WWXML.openDocument(xmlConfigStream);

        // The order of the following two calls is important, because building the writable location may entail
        // creating a location that's included in the specified read locations.
        final Element docElement = doc.getDocumentElement();
        this.buildWritePaths(docElement);
        this.buildReadPaths(docElement);

        if (this.writeLocation == null)
        {
            Logging.warning("FileStore.NoWriteLocation");
        }

        if (this.readLocations.size() == 0)
        {
            // This should not happen because the writable location is added to the read list, but check nonetheless
            String message = Logging.getMessage("FileStore.NoReadLocations");
            Logging.error(message);
            throw new IllegalStateException(message);
        }
    }

    protected void buildReadPaths(Element dataFileStoreNode)
    {
        XPath xpath = WWXML.makeXPath();

        List<Element> elements = WWXML.getElements(dataFileStoreNode, "/dataFileStore/readLocations/location", xpath);

        if (elements == null)
        {
            String message = Logging.getMessage("FileStore.ExceptionReadingConfigurationFile");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        for (Element location : elements)
        {
            String prop = WWXML.getText(location, "@property", xpath);
            String wwDir = WWXML.getText(location, "@wwDir", xpath);
            String append = WWXML.getText(location, "@append", xpath);
            String isInstall = WWXML.getText(location, "@isInstall", xpath);
            String isMarkWhenUsed = WWXML.getText(location, "@isMarkWhenUsed", xpath);

            String path = buildLocationPath(prop, append, wwDir);
            if (path == null)
            {
                Logging.warning(Logging.getMessage("FileStore.LocationInvalid",
                    prop != null ? prop : Logging.getMessage("generic.Unknown")));
                continue;
            }

            StoreLocation oldStore = this.storeLocationFor(path);
            if (oldStore != null) // filter out duplicates
                continue;

            // Even paths that don't exist or are otherwise problematic are added to the list because they may
            // become readable during the session. E.g., removable media. So add them to the search list.

            File pathFile = new File(path);
            if (pathFile.exists() && !pathFile.isDirectory())
            {
                Logging.warning(Logging.getMessage("FileStore.LocationIsFile", pathFile.getPath()));
            }

            boolean pathIsInstall = isInstall != null && (isInstall.contains("t") || isInstall.contains("T"));
            StoreLocation newStore = new StoreLocation(pathFile, pathIsInstall);

            // If the input parameter "markWhenUsed" is null or empty, then the StoreLocation should keep its
            // default value. Otherwise the store location value is set to true when the input parameter contains
            // "t", and is set to false otherwise.
            if (isMarkWhenUsed != null && isMarkWhenUsed.length() > 0)
                newStore.setMarkWhenUsed(isMarkWhenUsed.toLowerCase().contains("t"));

            this.readLocations.add(newStore);
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void buildWritePaths(Element dataFileCacheNode)
    {
        XPath xpath = WWXML.makeXPath();

        List<Element> elements = WWXML.getElements(dataFileCacheNode, "/dataFileStore/writeLocations/location", xpath);

        if (elements == null)
        {
            String message = Logging.getMessage("FileStore.ExceptionReadingConfigurationFile");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        for (Element location : elements)
        {
            String prop = WWXML.getText(location, "@property", xpath);
            String wwDir = WWXML.getText(location, "@wwDir", xpath);
            String append = WWXML.getText(location, "@append", xpath);
            String create = WWXML.getText(location, "@create", xpath);

            String path = buildLocationPath(prop, append, wwDir);
            if (path == null)
            {
                Logging.warning(Logging.getMessage("FileStore.LocationInvalid",
                    prop != null ? prop : Logging.getMessage("generic.Unknown")));
                continue;
            }

            Logging.verbose(Logging.getMessage("FileStore.AttemptingWriteDir", path));
            File pathFile = new File(path);
            if (!pathFile.exists() && create != null && (create.contains("t") || create.contains("T")))
            {
                Logging.verbose(Logging.getMessage("FileStore.MakingDirsFor", path));
                pathFile.mkdirs();
            }

            if (pathFile.isDirectory() && pathFile.canWrite() && pathFile.canRead())
            {
                Logging.verbose(Logging.getMessage("FileStore.WriteLocationSuccessful", path));
                this.writeLocation = new StoreLocation(pathFile);

                // Remove the writable location from search path if it already exists.
                StoreLocation oldLocation = this.storeLocationFor(path);
                if (oldLocation != null)
                    this.readLocations.remove(oldLocation);

                // Writable location is always first in search path.
                this.readLocations.add(0, this.writeLocation);

                break; // only need one
            }
        }
    }

    protected String buildLocationPath(String property, String append, String wwDir)
    {
        String path = propertyToPath(property);

        if (append != null && append.length() != 0)
            path = WWIO.appendPathPart(path, append.trim());

        if (wwDir != null && wwDir.length() != 0)
            path = WWIO.appendPathPart(path, wwDir.trim());

        return path;
    }

    protected String propertyToPath(String propName)
    {
        if (propName == null || propName.length() == 0)
            return null;

        return System.getProperty(propName);

        // TODO support platform default if prop is not set?
    }

    //**************************************************************//
    //********************  File Store Locations  ******************//
    //**************************************************************//

    public List<? extends java.io.File> getLocations()
    {
        ArrayList<File> locations = new ArrayList<java.io.File>();
        for (StoreLocation location : this.readLocations)
        {
            locations.add(location.getFile());
        }
        return locations;
    }

    public java.io.File getWriteLocation()
    {
        return (this.writeLocation != null) ? this.writeLocation.getFile() : null;
    }

    public void addLocation(String newPath, boolean isInstall)
    {
        this.addLocation(this.readLocations.size(), newPath, isInstall);
    }

    public void addLocation(int index, String newPath, boolean isInstall)
    {
        if (WWUtil.isEmpty(newPath))
        {
            String message = Logging.getMessage("nullValue.FileStorePathIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (index < 0)
        {
            String message = Logging.getMessage("generic.InvalidIndex", index);
            Logging.verbose(message);
            throw new IllegalArgumentException(message);
        }

        StoreLocation oldLocation = this.storeLocationFor(newPath);
        if (oldLocation != null)
            this.readLocations.remove(oldLocation);

        if (index > 0 && index > this.readLocations.size())
            index = this.readLocations.size();

        File newFile = new File(newPath);
        StoreLocation newLocation = new StoreLocation(newFile, isInstall);
        this.readLocations.add(index, newLocation);
    }

    public void removeLocation(String path)
    {
        if (path == null || path.length() == 0)
        {
            String message = Logging.getMessage("nullValue.FileStorePathIsNull");
            Logging.error(message);
            // Just warn and return.
            return;
        }

        StoreLocation location = this.storeLocationFor(path);
        if (location == null) // Path is not part of this FileStore.
            return;

        if (location.equals(this.writeLocation))
        {
            String message = Logging.getMessage("FileStore.CannotRemoveWriteLocation", path);
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        this.readLocations.remove(location);
    }

    protected StoreLocation storeLocationFor(String path)
    {
        java.io.File file = new java.io.File(path);

        for (StoreLocation location : this.readLocations)
        {
            if (file.equals(location.getFile()))
                return location;
        }

        return null;
    }

    //**************************************************************//
    //********************  File Store Contents  *******************//
    //**************************************************************//

    public boolean containsFile(String fileName)
    {
        if (fileName == null)
            return false;

        for (StoreLocation location : this.readLocations)
        {
            java.io.File dir = location.getFile();
            java.io.File file;

            if (fileName.startsWith(dir.getAbsolutePath()))
                file = new java.io.File(fileName);
            else
                file = new File(dir, fileName);

            if (file.exists())
                return true;
        }

        return false;
    }

    /**
     * @param fileName       the name of the file to find
     * @param checkClassPath if <code>true</code>, the class path is first searched for the file, otherwise the class
     *                       path is not searched unless it's one of the explicit paths in the cache search directories
     *
     * @return a handle to the requested file if it exists in the cache, otherwise null
     *
     * @throws IllegalArgumentException if <code>fileName</code> is null
     */
    public URL findFile(String fileName, boolean checkClassPath)
    {
        if (fileName == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (checkClassPath)
        {
            URL url = this.getClass().getClassLoader().getResource(fileName);
            if (url != null)
                return url;
        }

        for (StoreLocation location : this.readLocations)
        {
            File dir = location.getFile();
            if (!dir.exists())
                continue;

            File file = new File(dir, fileName);
            if (file.exists())
            {
                try
                {
                    if (location.isMarkWhenUsed())
                        markFileUsed(file);
                    else
                        markFileUsed(file.getParentFile());

                    return file.toURI().toURL();
                }
                catch (MalformedURLException e)
                {
                    Logging.error(Logging.getMessage("FileStore.ExceptionCreatingURLForFile", file.getPath()), e);
                }
            }
        }

        return null;
    }

    /**
     * @param fileName the name to give the newly created file
     *
     * @return a handle to the newly created file if it could be created and added to the file store, otherwise null
     *
     * @throws IllegalArgumentException if <code>fileName</code> is null
     */
    public java.io.File newFile(String fileName)
    {
        if (fileName == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (this.writeLocation != null)
        {
            File file = new File(this.writeLocation.getFile(), fileName);
            boolean canCreateFile = false;

            // This block of code must be synchronized for proper operation. A thread may check that
            // file.getParentFile() does not exist, and become immediately suspended. A second thread may then create
            // the parent and ancestor directories. When the first thread wakes up, file.getParentFile().mkdirs()
            // fails, resulting in an erroneous log message: The log reports that the file cannot be created.
            synchronized (this.fileLock)
            {
                if (file.getParentFile().exists())
                    canCreateFile = true;
                else if (file.getParentFile().mkdirs())
                    canCreateFile = true;
            }

            if (canCreateFile)
                return file;
            else
            {
                String msg = Logging.getMessage("generic.CannotCreateFile", file);
                Logging.error(msg);
            }
        }

        return null;
    }

    public void removeFile(URL url)
    {
        if (url == null)
        {
            String msg = Logging.getMessage("nullValue.URLIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        try
        {
            File file = new File(url.toURI());

            // This block of code must be synchronized for proper operation. A thread may check that the file exists,
            // and become immediately suspended. A second thread may then delete that file. When the first thread
            // wakes up, file.delete() fails.
            synchronized (this.fileLock)
            {
                if (file.exists())
                {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
        catch (URISyntaxException e)
        {
            Logging.error(Logging.getMessage("FileStore.ExceptionRemovingFile", url.toString()), e);
        }
    }

    @Override
    public String[] listFileNames(String pathName, FileStoreFilter filter)
    {
        // TODO Implement on Android
        return new String[0];
    }

    @Override
    public String[] listAllFileNames(String pathName, FileStoreFilter filter)
    {
        // TODO Implement on Android
        return new String[0];
    }

    @Override
    public String[] listTopFileNames(String pathName, FileStoreFilter filter)
    {
        // TODO Implement on Android
        return new String[0];
    }

    @SuppressWarnings( {"ResultOfMethodCallIgnored"})
    protected void markFileUsed(java.io.File file)
    {
        if (file == null)
            return;

        long currentTime = System.currentTimeMillis();

        if (file.canWrite())
            file.setLastModified(currentTime);

        if (file.isDirectory())
            return;

        java.io.File parent = file.getParentFile();
        if (parent != null && parent.canWrite())
            parent.setLastModified(currentTime);
    }
}
