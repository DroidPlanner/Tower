/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.layers;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.beans.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author dcollins
 * @version $Id$
 */
public class LayerList extends CopyOnWriteArrayList<Layer> implements WWObject
{
    protected WWObjectImpl wwo = new WWObjectImpl(this);

    public LayerList()
    {
    }

    public LayerList(Collection<? extends Layer> layers)
    {
        super(layers);
    }

    public LayerList(Layer[] layers)
    {
        super(layers);
    }

    public boolean add(Layer layer)
    {
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        LayerList copy = makeShallowCopy(this);
        super.add(layer);
        layer.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, copy, this);

        return true;
    }

    public void add(int index, Layer layer)
    {
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        LayerList copy = makeShallowCopy(this);
        super.add(index, layer);
        layer.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, copy, this);
    }

    public boolean addAll(Collection<? extends Layer> layers)
    {
        LayerList copy = makeShallowCopy(this);
        boolean added = super.addAll(layers);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        return added;
    }

    public boolean addAll(int i, Collection<? extends Layer> layers)
    {
        for (Layer layer : layers)
        {
            layer.addPropertyChangeListener(this);
        }

        LayerList copy = makeShallowCopy(this);
        boolean added = super.addAll(i, layers);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        return added;
    }

    public boolean addIfAbsent(Layer layer)
    {
        for (Layer l : this)
        {
            if (l.equals(layer))
                return false;
        }

        layer.addPropertyChangeListener(this);

        LayerList copy = makeShallowCopy(this);
        boolean added = super.addIfAbsent(layer);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        return added;
    }

    public int addAllAbsent(Collection<? extends Layer> layers)
    {
        for (Layer layer : layers)
        {
            if (!this.contains(layer))
                layer.addPropertyChangeListener(this);
        }

        LayerList copy = makeShallowCopy(this);
        int numAdded = super.addAllAbsent(layers);
        if (numAdded > 0)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        return numAdded;
    }

    public Layer set(int index, Layer layer)
    {
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Layer oldLayer = this.get(index);
        if (oldLayer != null)
            oldLayer.removePropertyChangeListener(this);

        LayerList copy = makeShallowCopy(this);
        super.set(index, layer);
        layer.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.LAYERS, copy, this);

        return oldLayer;
    }

    public void remove(Layer layer)
    {
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.contains(layer))
            return;

        LayerList copy = makeShallowCopy(this);
        layer.removePropertyChangeListener(this);
        super.remove(layer);
        this.firePropertyChange(AVKey.LAYERS, copy, this);
    }

    public boolean remove(Object o)
    {
        for (Layer layer : this)
        {
            if (layer.equals(o))
                layer.removePropertyChangeListener(this);
        }

        LayerList copy = makeShallowCopy(this);
        boolean removed = super.remove(o);
        if (removed)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        return removed;
    }

    public Layer remove(int index)
    {
        Layer layer = get(index);
        if (layer == null)
            return null;

        LayerList copy = makeShallowCopy(this);
        layer.removePropertyChangeListener(this);
        super.remove(index);
        this.firePropertyChange(AVKey.LAYERS, copy, this);

        return layer;
    }

    public boolean removeAll(Collection<?> objects)
    {
        for (Layer layer : this)
        {
            layer.removePropertyChangeListener(this);
        }

        LayerList copy = makeShallowCopy(this);
        boolean removed = super.removeAll(objects);
        if (removed)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        for (Layer layer : this)
        {
            layer.addPropertyChangeListener(this);
        }

        return removed;
    }

    public boolean removeAll()
    {
        for (Layer layer : this)
        {
            layer.removePropertyChangeListener(this);
        }

        LayerList copy = makeShallowCopy(this);
        boolean removed = super.retainAll(new ArrayList<Layer>()); // retain no layers
        if (removed)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        return removed;
    }

    @SuppressWarnings( {"SuspiciousMethodCalls"})
    public boolean retainAll(Collection<?> objects)
    {
        for (Layer layer : this)
        {
            if (!objects.contains(layer))
                layer.removePropertyChangeListener(this);
        }

        LayerList copy = makeShallowCopy(this);
        boolean added = super.retainAll(objects);
        if (added)
            this.firePropertyChange(AVKey.LAYERS, copy, this);

        return added;
    }

    public void replaceAll(Collection<? extends Layer> layers)
    {
        ArrayList<Layer> toDelete = new ArrayList<Layer>();
        ArrayList<Layer> toKeep = new ArrayList<Layer>();

        for (Layer layer : layers)
        {
            if (!this.contains(layer))
                toDelete.add(layer);
            else
                toKeep.add(layer);
        }

        for (Layer layer : toDelete)
        {
            this.remove(layer);
        }

        super.clear();

        for (Layer layer : layers)
        {
            if (!toKeep.contains(layer))
                layer.addPropertyChangeListener(this);

            super.add(layer);
        }
    }

    protected LayerList makeShallowCopy(LayerList sourceList)
    {
        return new LayerList(sourceList);
    }

    /** {@inheritDoc} */
    public Object getValue(String key)
    {
        return this.wwo.getValue(key);
    }

    /** {@inheritDoc} */
    public String getStringValue(String key)
    {
        return this.wwo.getStringValue(key);
    }

    /** {@inheritDoc} */
    public Object setValue(String key, Object value)
    {
        return this.wwo.setValue(key, value);
    }

    /** {@inheritDoc} */
    public Collection<Object> getValues()
    {
        return this.wwo.getValues();
    }

    /** {@inheritDoc} */
    public AVList setValues(AVList avList)
    {
        return this.wwo.setValues(avList);
    }

    /** {@inheritDoc} */
    public Set<Map.Entry<String, Object>> getEntries()
    {
        return this.wwo.getEntries();
    }

    /** {@inheritDoc} */
    public boolean hasKey(String key)
    {
        return this.wwo.hasKey(key);
    }

    /** {@inheritDoc} */
    public Object removeKey(String key)
    {
        return this.wwo.removeKey(key);
    }

    /** {@inheritDoc} */
    public AVList copy()
    {
        return this.wwo.copy();
    }

    /** {@inheritDoc} */
    public AVList clearList()
    {
        return this.wwo.clearList();
    }

    /** {@inheritDoc} */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.wwo.addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.wwo.removePropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.wwo.addPropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.wwo.removePropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.wwo.firePropertyChange(propertyName, oldValue, newValue);
    }

    /** {@inheritDoc} */
    public void firePropertyChange(PropertyChangeEvent event)
    {
        this.wwo.firePropertyChange(event);
    }

    /** {@inheritDoc} */
    public void propertyChange(PropertyChangeEvent event)
    {
        this.wwo.propertyChange(event);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.size(); i++)
        {
            if (i > 0)
                sb.append(", ");
            sb.append(this.get(i));
        }

        return sb.toString();
    }

    /**
     * Aggregate the contents of a group of layer lists into a single one. All layers are placed in the first designated
     * list and removed from the subsequent lists.
     *
     * @param lists an array containing the lists to aggregate. All members of the second and subsequent lists in the
     *              array are added to the first list in the array.
     *
     * @return the aggregated list.
     *
     * @throws IllegalArgumentException if the layer-lists array is null or empty.
     */
    public static LayerList collapseLists(LayerList[] lists)
    {
        if (lists == null || lists.length == 0)
        {
            String msg = Logging.getMessage("nullValue.LayerListArrayIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        LayerList list = lists[0];

        for (int i = 1; i < lists.length; i++)
        {
            LayerList ll = lists[i];

            for (Layer layer : ll)
            {
                list.add(layer);
            }

            for (Layer layer : ll)
            {
                ll.remove(layer);
            }
        }

        return list;
    }
}
