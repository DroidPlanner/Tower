/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import android.util.Log;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.Configuration;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;

import java.text.MessageFormat;
import java.util.*;

/**
 * This class of static methods provides the interface to logging for World Wind components. Logging is performed via
 * {@link java.util.logging}. The default logger name is <code>gov.nasa.worldwind</code>. The logger name is
 * configurable via {@link Configuration}.
 *
 * @author tag
 * @version $Id$
 * @see java.util.logging
 */
public class Logging
{
    protected static final String MESSAGE_BUNDLE_NAME = Logging.class.getPackage().getName() + ".MessageStrings";
    protected static final int MAX_MESSAGE_REPEAT = Configuration.getIntegerValue(AVKey.MAX_MESSAGE_REPEAT);

    // Singleton, prevent public instantiation.
    protected Logging()
    {
    }

    protected static String getLogcatTag()
    {
        try
        {
            // The Configuration singleton may not be established yet, so catch the exception that occurs if it's not
            // and use the default logger name.
            return Configuration.getStringValue(AVKey.LOGCAT_TAG);
        }
        catch (Exception e)
        {
            return Configuration.DEFAULT_LOGCAT_TAG;
        }
    }

    public static void verbose(String msg)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.VERBOSE))
        {
            Log.v(tag, msg);
        }
    }

    public static void verbose(String msg, Throwable tr)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.VERBOSE))
        {
            Log.v(tag, msg, tr);
        }
    }

    public static void debug(String msg)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.DEBUG))
        {
            Log.d(tag, msg);
        }
    }

    public static void debug(String msg, Throwable tr)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.DEBUG))
        {
            Log.d(tag, msg, tr);
        }
    }

    public static void info(String msg)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.INFO))
        {
            Log.i(tag, msg);
        }
    }

    public static void info(String msg, Throwable tr)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.INFO))
        {
            Log.i(tag, msg, tr);
        }
    }

    public static void warning(String msg)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.WARN))
        {
            Log.w(tag, msg);
        }
    }

    public static void warning(String msg, Throwable tr)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.WARN))
        {
            Log.w(tag, msg, tr);
        }
    }

    public static void error(String msg)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.ERROR))
        {
            Log.e(tag, msg);
        }
    }

    public static void error(String msg, Throwable tr)
    {
        String tag = getLogcatTag();
        if (Log.isLoggable(tag, Log.ERROR))
        {
            Log.e(tag, msg, tr);
        }
    }

    public static void warning(String tag, String msg)
    {
        if (Log.isLoggable(tag, Log.WARN))
        {
            Log.w(tag, msg);
        }
    }

    public static void error(String tag, String msg)
    {
        if (Log.isLoggable(tag, Log.ERROR))
        {
            Log.e(tag, msg);
        }
    }

    /**
     * Retrieves a message from the World Wind message resource bundle.
     *
     * @param property the property identifying which message to retrieve.
     *
     * @return The requested message.
     */
    public static String getMessage(String property)
    {
        try
        {
            return (String) ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, Locale.getDefault()).getObject(property);
        }
        catch (Exception e)
        {
            String msg = "Exception looking up message from bundle " + MESSAGE_BUNDLE_NAME;
            error(msg, e);
            return msg;
        }
    }

    /**
     * Retrieves a message from the World Wind message resource bundle formatted with specified arguments. The arguments
     * are inserted into the message via {@link java.text.MessageFormat}.
     *
     * @param property the property identifying which message to retrieve.
     * @param args     the arguments referenced by the format string identified <code>property</code>.
     *
     * @return The requested string formatted with the arguments.
     *
     * @see java.text.MessageFormat
     */
    public static String getMessage(String property, Object... args)
    {
        String msg;

        try
        {
            msg = (String) ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, Locale.getDefault()).getObject(property);
        }
        catch (Exception e)
        {
            msg = "Exception looking up message from bundle " + MESSAGE_BUNDLE_NAME;
            error(msg, e);
            return msg;
        }

        try
        {
            // TODO: This is no longer working with more than one arg in the message string, e.g., {1}
            return args == null ? msg : MessageFormat.format(msg, args);
        }
        catch (IllegalArgumentException e)
        {
            msg = "Message arguments do not match format string: " + property;
            error(msg, e);
            return msg;
        }
    }

    /**
     * Indicates the maximum number of times the same log message should be repeated when generated in the same context,
     * such as within a loop over renderables when operations in the loop consistently fail.
     *
     * @return the maximum number of times to repeat a message.
     */
    public static int getMaxMessageRepeatCount()
    {
        return MAX_MESSAGE_REPEAT;
    }
}
