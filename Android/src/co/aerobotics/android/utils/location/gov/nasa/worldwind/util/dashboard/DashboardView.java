/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util.dashboard;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.WorldWindowGLSurfaceView;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.event.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.PerformanceStatistic;

import java.util.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class DashboardView extends LinearLayout implements RenderingListener
{
    protected WorldWindowGLSurfaceView wwd;
    protected BasicListAdapter<PerformanceStatistic> perfStatListAdapter;
    protected List<PerformanceStatistic> perfStats = new ArrayList<PerformanceStatistic>();
    protected Comparator<PerformanceStatistic> perfStatComparator = new Comparator<PerformanceStatistic>()
    {
        public int compare(PerformanceStatistic a, PerformanceStatistic b)
        {
            return a.getDisplayName().compareTo(b.getDisplayName());
        }
    };
    protected final Object perfStatLock = new Object();

    public DashboardView(Context context)
    {
        super(context);

        this.init(context);
    }

    public DashboardView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.init(context);
    }

    public DashboardView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        this.init(context);
    }

    protected void init(Context context)
    {
        // Configure the dashboard to be invisible and not take up any layout space by default.
        this.setVisibility(View.GONE);
        this.setOrientation(LinearLayout.VERTICAL);

        this.perfStatListAdapter = new BasicListAdapter<PerformanceStatistic>(context);
        ListView lv = new ListView(context);
        lv.setAdapter(this.perfStatListAdapter);
        this.addView(lv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1));

        CheckBox cb = new CheckBox(context);
        cb.setText("Run Continuously");
        cb.setOnClickListener(new OnClickListener()
        {
            public void onClick(View view)
            {
                int renderMode = ((CheckBox) view).isChecked() ? GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    : GLSurfaceView.RENDERMODE_WHEN_DIRTY;
                wwd.setRenderMode(renderMode);
            }
        });
        this.addView(cb, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0));

        Button btn = new Button(context);
        btn.setText("Hide");
        btn.setOnClickListener(new OnClickListener()
        {
            public void onClick(View view)
            {
                setVisibility(View.GONE);
            }
        });
        this.addView(btn, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0));
    }

    public WorldWindowGLSurfaceView getWwd()
    {
        return this.wwd;
    }

    public void setWwd(WorldWindowGLSurfaceView wwd)
    {
        if (this.wwd != null)
            this.wwd.removeRenderingListener(this);
        if (wwd != null)
            wwd.addRenderingListener(this);

        this.wwd = wwd;
    }

    public void stageChanged(RenderingEvent event)
    {
        //noinspection StringEquality
        if (event != null && event.getStage() == RenderingEvent.AFTER_RENDERING)
        {
            this.afterRendering();
        }
    }

    protected void afterRendering()
    {
        synchronized (this.perfStatLock)
        {
            this.perfStats.clear();
            this.perfStats.addAll(this.wwd.getSceneController().getPerFrameStatistics());
            Collections.sort(this.perfStats, this.perfStatComparator);
        }

        Activity activity = (Activity) this.getContext();
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                synchronized (perfStatLock)
                {
                    perfStatListAdapter.setValues(perfStats);
                }
            }
        });
    }
}
