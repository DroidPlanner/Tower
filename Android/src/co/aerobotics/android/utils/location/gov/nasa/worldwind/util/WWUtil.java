/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import java.lang.reflect.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class WWUtil
{
    /**
     * Determine whether an object reference is null or a reference to an empty string.
     *
     * @param s the reference to examine.
     *
     * @return true if the reference is null or is a zero-length {@link String}.
     */
    public static boolean isEmpty(Object s)
    {
        return s == null || (s instanceof String && ((String) s).length() == 0);
    }

    /**
     * Creates a two-element array of default min and max values, typically used to initialize extreme values searches.
     *
     * @return a two-element array of extreme values. Entry 0 is the maximum double value; entry 1 is the negative of
     *         the maximum double value;
     */
    public static double[] defaultMinMix()
    {
        return new double[] {Double.MAX_VALUE, -Double.MAX_VALUE};
    }

    /**
     * Parses a string to an integer value if the string can be parsed as a integer. Does not log a message if the
     * string can not be parsed as an integer.
     *
     * @param s the string to parse.
     *
     * @return the integer value parsed from the string, or null if the string cannot be parsed as an integer.
     */
    public static Integer makeInteger(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a long value if the string can be parsed as a long. Does not log a message if the string can
     * not be parsed as a long.
     *
     * @param s the string to parse.
     *
     * @return the long value parsed from the string, or null if the string cannot be parsed as a long.
     */
    public static Long makeLong(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Long.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a double value if the string can be parsed as a double. Does not log a message if the string
     * can not be parsed as a double.
     *
     * @param s the string to parse.
     *
     * @return the double value parsed from the string, or null if the string cannot be parsed as a double.
     */
    public static Double makeDouble(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a boolean value if the string can be parsed as a boolean. Does not log a message if the string
     * can not be parsed as a boolean.
     *
     * @param s the string to parse.
     *
     * @return the boolean value parsed from the string, or null if the string cannot be parsed as a boolean.
     */
    public static Boolean makeBoolean(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Boolean.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Uses reflection to invoke a <i>set</i> method for a specified property. The specified class must have a method
     * named "set" + propertyName, with either a single <code>String</code> argument, a single <code>double</code>
     * argument, a single <code>int</code> argument or a single <code>long</code> argument. If it does, the method is
     * called with the specified property value argument.
     *
     * @param parent        the object on which to set the property.
     * @param propertyName  the name of the property.
     * @param propertyValue the value to give the property. Specify double, int and long values in a
     *                      <code>String</code>.
     *
     * @return the return value of the <i>set</i> method, or null if the method has no return value.
     *
     * @throws IllegalArgumentException if the parent object or the property name is null.
     * @throws NoSuchMethodException    if no <i>set</i> method exists for the property name.
     * @throws java.lang.reflect.InvocationTargetException
     *                                  if the <i>set</i> method throws an exception.
     * @throws IllegalAccessException   if the <i>set</i> method is inaccessible due to access control.
     */
    public static Object invokePropertyMethod(Object parent, String propertyName, String propertyValue)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        if (parent == null)
        {
            String msg = Logging.getMessage("nullValue.ParentIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String methodName = "set" + propertyName;

        try // String arg
        {
            Method method = parent.getClass().getMethod(methodName, new Class[] {String.class});
            return method != null ? method.invoke(parent, propertyValue) : null;
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // double arg
        {
            Double d = WWUtil.makeDouble(propertyValue);
            if (d != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {double.class});
                return method != null ? method.invoke(parent, d) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // int arg
        {
            Integer i = WWUtil.makeInteger(propertyValue);
            if (i != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {int.class});
                return method != null ? method.invoke(parent, i) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // boolean arg
        {
            Boolean b = WWUtil.makeBoolean(propertyValue);
            if (b != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {boolean.class});
                return method != null ? method.invoke(parent, b) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // long arg
        {
            Long l = WWUtil.makeLong(propertyValue);
            if (l != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {long.class});
                return method != null ? method.invoke(parent, l) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        throw new NoSuchMethodException();
    }

    /**
     * Eliminates all white space in a specified string. (Applies the regular expression "\\s+".)
     *
     * @param inputString the string to remove white space from.
     *
     * @return the string with white space eliminated, or null if the input string is null.
     */
    public static String removeWhiteSpace(String inputString)
    {
        if (WWUtil.isEmpty(inputString))
        {
            return inputString;
        }

        return inputString.replaceAll("\\s+", "");
    }
}
