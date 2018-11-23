/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util.dashboard;

import android.content.Context;
import android.database.*;
import android.view.*;
import android.widget.*;

import java.util.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class BasicListAdapter<T> implements ListAdapter
{
    protected Context context;
    protected List<T> values = new ArrayList<T>();
    protected DataSetObservable dataSetObservable = new DataSetObservable();

    public BasicListAdapter(Context context)
    {
        this.context = context;
    }

    public Collection<T> getValues()
    {
        return this.values;
    }

    public void setValues(Collection<? extends T> c)
    {
        this.values.clear();

        if (c != null)
            this.values.addAll(c);

        this.dataSetObservable.notifyChanged();
    }

    public int getCount()
    {
        return this.values.size();
    }

    public Object getItem(int position)
    {
        return this.values.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public int getItemViewType(int position)
    {
        return 0; // All views share the same type.
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        TextView view;

        if (convertView instanceof TextView)
        {
            view = (TextView) convertView;
        }
        else
        {
            view = this.createView();
        }

        this.applyItemToView(position, view);

        return view;
    }

    protected TextView createView()
    {
        TextView view = new TextView(this.context);
        view.setPadding(5, 5, 5, 5);

        return view;
    }

    protected void applyItemToView(int position, TextView view)
    {
        Object o = this.values.get(position);
        view.setText(o.toString());
    }

    public int getViewTypeCount()
    {
        return 1; // All views share the same type.
    }

    public boolean hasStableIds()
    {
        return false; // The same ID does not always refer to the same object.
    }

    public boolean isEmpty()
    {
        return this.values.isEmpty();
    }

    public void registerDataSetObserver(DataSetObserver observer)
    {
        this.dataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer)
    {
        this.dataSetObservable.unregisterObserver(observer);
    }

    public boolean areAllItemsEnabled()
    {
        return true; // The performance statistics are always enabled.
    }

    public boolean isEnabled(int position)
    {
        return true; // The performance statistics are always enabled.
    }
}
