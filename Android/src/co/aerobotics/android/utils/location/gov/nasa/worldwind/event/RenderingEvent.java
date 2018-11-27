/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.event;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class RenderingEvent extends WWEvent
{
    public static final String BEFORE_RENDERING = "gov.nasa.worldwind.RenderingEvent.BeforeRendering";
    public static final String AFTER_RENDERING = "gov.nasa.worldwind.RenderingEvent.AfterRendering";

    protected String stage;

    public RenderingEvent(Object source, String stage)
    {
        super(source);
        this.stage = stage;
    }

    public String getStage()
    {
        return this.stage != null ? this.stage : "gov.nasa.worldwind.RenderingEvent.UnknownStage";
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append(" ");
        sb.append(this.stage != null ? this.stage : Logging.getMessage("term.Unknown"));

        return sb.toString();
    }
}
