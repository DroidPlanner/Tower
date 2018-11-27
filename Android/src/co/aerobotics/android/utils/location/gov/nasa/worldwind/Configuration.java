/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.WWUtil;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.WWXML;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.util.*;

/**
 * This class manages the initial World Wind configuration. It reads World Wind configuration files and registers their
 * contents. Configurations files contain the names of classes to create at run-time, the initial model definition,
 * including the globe, elevation model and layers, and various control quantities such as cache sizes and data
 * retrieval timeouts.
 * <p/>
 * The Configuration class is a singleton, but its instance is not exposed publicly. It is addressed only via static
 * methods of the class. It is constructed upon first use of any of its static methods.
 * <p/>
 * When the Configuration class is first instantiated it reads the XML document <code>config/worldwind.xml</code> and
 * registers all the information there. The information can subsequently be retrieved via the class' various
 * <code>getValue</code> methods. Many World Wind start-up objects query this information to determine the classes to
 * create. For example, the first World Wind object created by an application is typically a {@link
 * WorldWindowGLSurfaceView}. During construction that class causes World Wind's internal classes to
 * be constructed, using the names of those classes drawn from the Configuration singleton, this class.
 * <p/>
 * The default World Wind configuration document is <code>config/worldwind.xml</code>. This can be changed by setting
 * the Java property <code>gov.nasa.worldwind.config.document</code> to a different file name or a valid URL prior to
 * creating any World Wind object or invoking any static methods of World Wind classes, including the Configuration
 * class. When an application specifies a different configuration location it typically does so in its main method prior
 * to using World Wind. If a file is specified its location must be on the classpath. (The contents of application and
 * World Wind jar files are typically on the classpath, in which case the configuration file may be in the jar file.)
 * <p/>
 * Additionally, an application may set another Java property, <code>gov.nasa.worldwind.app.config.document</code>, to a
 * file name or URL whose contents contain configuration values to override those of the primary configuration document.
 * World Wind overrides only those values in this application document, it leaves all others to the value specified in
 * the primary document. Applications usually specify an override document in order to specify the initial layers in the
 * model.
 * <p/>
 * See <code>config/worldwind.xml</code> for documentation on setting configuration values.
 * <p/>
 * Configuration values can also be set programatically via {@link #setValue(String, Object)}, but they are not
 * retroactive so affect only Configuration queries made subsequent to setting the value.
 *
 * @author dcollins
 * @version $Id$
 */
public class Configuration // Singleton
{
    public static final String DEFAULT_LOGCAT_TAG = "gov.nasa.worldwind";

    protected static final String CONFIG_APP_DOCUMENT_KEY = "gov.nasa.worldwind.app.config.document";
    protected static final String CONFIG_WW_DOCUMENT_KEY = "gov.nasa.worldwind.config.document";
    protected static final String CONFIG_WW_DOCUMENT_DEFAULT_LOCATION = "config/worldwind.xml";

    protected static Configuration instance = new Configuration();

    protected static Configuration getInstance()
    {
        return instance;
    }

    protected Properties properties;
    protected List<Document> configDocs = new ArrayList<Document>();

    // Singleton, prevent public instantiation.
    protected Configuration()
    {
        // IMPORTANT NOTE: Always use the single argument version of Logging.logger in this method because the non-arg
        // method assumes an instance of Configuration already exists. This constructor is called during construction of
        // Configuration's singleton instance, so using the no-argument version of Logging.logger() here causes infinite
        // recursion.

        this.properties = this.initializeDefaults();

        // Load the application configuration (if there is one) before loading the primary World Wind configuration. This
        // gives the application configuration precedence over the the primary World Wind configuration.
        String appConfigLocation = System.getProperty(CONFIG_APP_DOCUMENT_KEY);
        if (appConfigLocation != null)
        {
            try
            {
                this.loadConfigDoc(appConfigLocation);
            }
            catch (Exception e)
            {
                // Don't stop if the application config file can't be found or parsed. Just log a warning and attempt
                // to parse the primary World Wind configuration.
                Logging.warning(DEFAULT_LOGCAT_TAG,
                    Logging.getMessage("Configuration.AppConfigNotFound", appConfigLocation));
            }
        }

        // Load the primary World Wind configuration.
        String wwConfigLocation = System.getProperty(CONFIG_WW_DOCUMENT_KEY, CONFIG_WW_DOCUMENT_DEFAULT_LOCATION);
        if (wwConfigLocation != null)
        {
            try
            {
                this.loadConfigDoc(wwConfigLocation);
            }
            catch (Exception e)
            {
                // Don't stop if the primary World Wind config file can't be found or parsed.
                Logging.warning(DEFAULT_LOGCAT_TAG,
                    Logging.getMessage("Configuration.PrimaryConfigNotFound", wwConfigLocation));
            }
        }
        else
        {
            // This should never happen, but we check anyway.
            Logging.warning(DEFAULT_LOGCAT_TAG, Logging.getMessage("Configuration.PrimaryConfigNotSpecified"));
        }

        // Load the config document properties from last to first, thereby giving the application configuration
        // precedence over the primary World Wind configuration.
        for (int i = this.configDocs.size() - 1; i >= 0; i--)
        {
            this.loadConfigProperties(this.configDocs.get(i));
        }
    }

    protected void loadConfigDoc(String configLocation)
    {
        if (!WWUtil.isEmpty(configLocation))
        {
            Document doc = WWXML.openDocument(configLocation);
            if (doc != null)
            {
                this.configDocs.add(doc);
            }
        }
    }

    protected void insertConfigDoc(String configLocation)
    {
        if (!WWUtil.isEmpty(configLocation))
        {
            Document doc = WWXML.openDocument(configLocation);
            if (doc != null)
            {
                this.configDocs.add(0, doc);
                this.loadConfigProperties(doc);
            }
        }
    }

    protected void loadConfigProperties(Document doc)
    {
        // IMPORTANT NOTE: Always use the single argument version of Logging.logger in this method because the non-arg
        // method assumes an instance of Configuration already exists. This method is called during construction of
        // Configuration's singleton instance, so using the no-argument version of Logging.logger() here causes infinite
        // recursion.

        try
        {
            XPath xpath = WWXML.makeXPath();

            NodeList nodes = (NodeList) xpath.evaluate("/WorldWindConfiguration/Property", doc, XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return;

            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                String name = xpath.evaluate("@name", node);
                String value = xpath.evaluate("@value", node);
                if (WWUtil.isEmpty(name))
                    continue;

                this.properties.setProperty(name, value);
            }
        }
        catch (XPathExpressionException e)
        {
            Logging.warning(DEFAULT_LOGCAT_TAG, Logging.getMessage("XML.ParserConfigurationException"));
        }
    }

    protected Properties initializeDefaults()
    {
        Properties defaults = new Properties();

        TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
        if (tz != null)
        {
            defaults.setProperty(AVKey.INITIAL_LONGITUDE,
                Double.toString(180.0 * tz.getOffset(System.currentTimeMillis()) / (12.0 * 3.6e6)));
        }

        return defaults;
    }

    public static void insertConfigurationDocument(String fileName)
    {
        getInstance().insertConfigDoc(fileName);
    }

    /**
     * Return as a string the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist.
     */
    public static synchronized String getStringValue(String key, String defaultValue)
    {
        String v = getStringValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as a string the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist.
     */
    public static synchronized String getStringValue(String key)
    {
        Object o = getInstance().properties.getProperty(key);
        return o != null ? o.toString() : null;
    }

    /**
     * Return as an Integer the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not an
     *         Integer or string representation of an Integer.
     */
    public static synchronized Integer getIntegerValue(String key, Integer defaultValue)
    {
        Integer v = getIntegerValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as an Integer the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not an Integer or string
     *         representation of an Integer.
     */
    public static synchronized Integer getIntegerValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Integer.parseInt(v);
        }
        catch (NumberFormatException e)
        {
            Logging.error(DEFAULT_LOGCAT_TAG, Logging.getMessage("generic.ConversionError", v));
            return null;
        }
    }

    /**
     * Return as an Long the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not a
     *         Long or string representation of a Long.
     */
    public static synchronized Long getLongValue(String key, Long defaultValue)
    {
        Long v = getLongValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as an Long the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not a Long or string
     *         representation of a Long.
     */
    public static synchronized Long getLongValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Long.parseLong(v);
        }
        catch (NumberFormatException e)
        {
            Logging.error(DEFAULT_LOGCAT_TAG, Logging.getMessage("generic.ConversionError", v));
            return null;
        }
    }

    /**
     * Return as an Double the value associated with a specified key.
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not an
     *         Double or string representation of an Double.
     */
    public static synchronized Double getDoubleValue(String key, Double defaultValue)
    {
        Double v = getDoubleValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as an Double the value associated with a specified key.
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not an Double or string
     *         representation of an Double.
     */
    public static synchronized Double getDoubleValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Double.parseDouble(v);
        }
        catch (NumberFormatException e)
        {
            Logging.error(DEFAULT_LOGCAT_TAG, Logging.getMessage("generic.ConversionError", v));
            return null;
        }
    }

    /**
     * Return as a Boolean the value associated with a specified key.
     * <p/>
     * Valid values for true are '1' or anything that starts with 't' or 'T'. ie. 'true', 'True', 't' Valid values for
     * false are '0' or anything that starts with 'f' or 'F'. ie. 'false', 'False', 'f'
     *
     * @param key          the key for the desired value.
     * @param defaultValue the value to return if the key does not exist.
     *
     * @return the value associated with the key, or the specified default value if the key does not exist or is not a
     *         Boolean or string representation of an Boolean.
     */
    public static synchronized Boolean getBooleanValue(String key, Boolean defaultValue)
    {
        Boolean v = getBooleanValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * Return as a Boolean the value associated with a specified key.
     * <p/>
     * Valid values for true are '1' or anything that starts with 't' or 'T'. ie. 'true', 'True', 't' Valid values for
     * false are '0' or anything that starts with 'f' or 'F'. ie. 'false', 'False', 'f'
     *
     * @param key the key for the desired value.
     *
     * @return the value associated with the key, or null if the key does not exist or is not a Boolean or string
     *         representation of an Boolean.
     */
    public static synchronized Boolean getBooleanValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        if (v.trim().toUpperCase().startsWith("T") || v.trim().equals("1"))
        {
            return true;
        }
        else if (v.trim().toUpperCase().startsWith("F") || v.trim().equals("0"))
        {
            return false;
        }
        else
        {
            Logging.error(DEFAULT_LOGCAT_TAG, Logging.getMessage("generic.ConversionError", v));
            return null;
        }
    }

    /**
     * Determines whether a key exists in the configuration.
     *
     * @param key the key of interest.
     *
     * @return true if the key exists, otherwise false.
     */
    public static synchronized boolean hasKey(String key)
    {
        return getInstance().properties.contains(key);
    }

    /**
     * Removes a key and its value from the configuration if the configuration contains the key.
     *
     * @param key the key of interest.
     */
    public static synchronized void removeKey(String key)
    {
        getInstance().properties.remove(key);
    }

    /**
     * Adds a key and value to the configuration, or changes the value associated with the key if the key is already in
     * the configuration.
     *
     * @param key   the key to set.
     * @param value the value to associate with the key.
     */
    public static synchronized void setValue(String key, Object value)
    {
        getInstance().properties.put(key, value.toString());
    }

    /**
     * Returns a specified element of an XML configuration document.
     *
     * @param xpathExpression an XPath expression identifying the element of interest.
     *
     * @return the element of interest if the XPath expression is valid and the element exists, otherwise null.
     *
     * @throws NullPointerException if the XPath expression is null.
     */
    public static Element getElement(String xpathExpression)
    {
        XPath xpath = WWXML.makeXPath();

        for (Document doc : getInstance().configDocs)
        {
            try
            {
                Node node = (Node) xpath.evaluate(xpathExpression, doc.getDocumentElement(), XPathConstants.NODE);
                if (node != null)
                    return (Element) node;
            }
            catch (XPathExpressionException e)
            {
                return null;
            }
        }

        return null;
    }
}
