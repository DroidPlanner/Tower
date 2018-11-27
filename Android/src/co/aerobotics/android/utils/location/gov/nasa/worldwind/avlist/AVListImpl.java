/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWRuntimeException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

import java.beans.*;
import java.util.*;

/**
 * An implementation class for the {@link AVList} interface. Classes implementing <code>AVList</code> can subclass or
 * aggregate this class to provide default <code>AVList</code> functionality. This class maintains a hash table of
 * attribute-value pairs.
 * <p/>
 * This class implements a notification mechanism for attribute-value changes. The mechanism provides a means for
 * objects to observe attribute changes or queries for certain keys without explicitly monitoring all keys. See {@link
 * java.beans.PropertyChangeSupport}.
 *
 * @author dcollins
 * @version $Id$
 */
public class AVListImpl implements AVList
{
    // Identifies the property change support instance in the avlist
    protected static final String PROPERTY_CHANGE_SUPPORT = "avlist.PropertyChangeSupport";

    // To avoid unnecessary overhead, this object's hash map is created only if needed.
    protected Map<String, Object> avList;

    /** Creates an empty attribute-value list. */
    public AVListImpl()
    {
    }

    /**
     * Constructor enabling aggregation.
     *
     * @param sourceBean The bean to be given as the source for any events.
     */
    public AVListImpl(Object sourceBean)
    {
        if (sourceBean != null)
            this.setValue(PROPERTY_CHANGE_SUPPORT, new PropertyChangeSupport(sourceBean));
    }

    public synchronized Object getValue(String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.hasAvList())
            return this.avList.get(key);

        return null;
    }

    public synchronized String getStringValue(String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalStateException(msg);
        }
        try
        {
            return (String) this.getValue(key);
        }
        catch (ClassCastException e)
        {
            String msg = Logging.getMessage("generic.ValueForKeyIsNotAString", key, this.getValue(key));
            Logging.error(msg);
            throw new WWRuntimeException(msg, e);
        }
    }

    public synchronized Object setValue(String key, Object value)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.avList(true).put(key, value);
    }

    public synchronized Collection<Object> getValues()
    {
        return this.hasAvList() ? this.avList.values() : this.createAvList().values();
    }

    public synchronized AVList setValues(AVList list)
    {
        if (list == null)
        {
            String msg = Logging.getMessage("nullValue.ListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Set<Map.Entry<String, Object>> entries = list.getEntries();
        for (Map.Entry<String, Object> entry : entries)
        {
            this.setValue(entry.getKey(), entry.getValue());
        }

        return this;
    }

    public synchronized Set<Map.Entry<String, Object>> getEntries()
    {
        return this.hasAvList() ? this.avList.entrySet() : this.createAvList().entrySet();
    }

    public synchronized boolean hasKey(String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.hasAvList() && this.avList.containsKey(key);
    }

    public synchronized Object removeKey(String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.hasKey(key) ? this.avList.remove(key) : null;
    }

    public synchronized AVList copy()
    {
        AVListImpl clone = new AVListImpl();

        if (this.avList != null)
        {
            clone.createAvList();
            clone.avList.putAll(this.avList);
        }

        return clone;
    }

    public synchronized AVList clearList()
    {
        if (this.hasAvList())
            this.avList.clear();

        return this;
    }

    protected boolean hasAvList()
    {
        return this.avList != null;
    }

    protected Map<String, Object> createAvList()
    {
        if (!this.hasAvList())
        {
            // The map type used must accept null values. java.util.concurrent.ConcurrentHashMap does not.
            this.avList = new HashMap<String, Object>();
        }

        return this.avList;
    }

    protected Map<String, Object> avList(boolean createIfNone)
    {
        if (createIfNone && !this.hasAvList())
            this.createAvList();

        return this.avList;
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.getChangeSupport().addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.getChangeSupport().removePropertyChangeListener(propertyName, listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.getChangeSupport().addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.getChangeSupport().removePropertyChangeListener(listener);
    }

    public synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        if (propertyName == null)
        {
            String msg = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.getChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    public synchronized void firePropertyChange(PropertyChangeEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.getChangeSupport().firePropertyChange(event);
    }

    protected synchronized PropertyChangeSupport getChangeSupport()
    {
        Object pcs = this.getValue(PROPERTY_CHANGE_SUPPORT);
        if (pcs == null || !(pcs instanceof PropertyChangeSupport))
        {
            pcs = new PropertyChangeSupport(this);
            this.setValue(PROPERTY_CHANGE_SUPPORT, pcs);
        }

        return (PropertyChangeSupport) pcs;
    }

    ///////////////////////////////////////
    // Static AVList utilities.
    ///////////////////////////////////////

    public static String getStringValue(AVList avList, String key, String defaultValue)
    {
        String v = getStringValue(avList, key);
        return v != null ? v : defaultValue;
    }

    public static String getStringValue(AVList avList, String key)
    {
        try
        {
            return avList.getStringValue(key);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static Integer getIntegerValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Integer)
            return (Integer) o;

        if (!(o instanceof String))
            return null;

        try
        {
            return Integer.parseInt((String) o);
        }
        catch (NumberFormatException e)
        {
            Logging.error(Logging.getMessage("generic.ConversionError", o));
            return null;
        }
    }

    public static Integer getIntegerValue(AVList avList, String key, Integer defaultValue)
    {
        Integer v = getIntegerValue(avList, key);
        return v != null ? v : defaultValue;
    }

    public static Long getLongValue(AVList avList, String key, Long defaultValue)
    {
        Long v = getLongValue(avList, key);
        return v != null ? v : defaultValue;
    }

    public static Long getLongValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Long)
            return (Long) o;

        String v = getStringValue(avList, key);
        if (v == null)
            return null;

        try
        {
            return Long.parseLong(v);
        }
        catch (NumberFormatException e)
        {
            Logging.error("Configuration.ConversionError", v);
            return null;
        }
    }

    public static Double getDoubleValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Double)
            return (Double) o;

        String v = getStringValue(avList, key);
        if (v == null)
            return null;

        try
        {
            return Double.parseDouble(v);
        }
        catch (NumberFormatException e)
        {
            Logging.error(Logging.getMessage("Configuration.ConversionError", v));
            return null;
        }
    }

    public void getRestorableStateForAVPair(String key, Object value, RestorableSupport rs,
        RestorableSupport.StateObject context)
    {
        if (value == null)
            return;

        if (key.equals(PROPERTY_CHANGE_SUPPORT))
            return;

        if (rs == null)
        {
            String message = Logging.getMessage("nullValue.RestorableStateIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        rs.addStateValueAsString(context, key, value.toString());
    }
}
