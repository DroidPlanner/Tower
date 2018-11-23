/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Globe;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.layers.*;
import org.w3c.dom.Element;

/**
 * @author dcollins
 * @version $Id$
 */
public class BasicModel extends WWObjectImpl implements Model
{
    protected Globe globe;
    protected LayerList layers;

    public BasicModel()
    {
        this.setGlobe(this.createGlobe());
        this.setLayers(this.createLayers());
    }

    public BasicModel(Globe globe, LayerList layers)
    {
        this.setGlobe(globe);
        this.setLayers(layers);
    }

    protected Globe createGlobe()
    {
        return (Globe) WorldWind.createConfigurationComponent(AVKey.GLOBE_CLASS_NAME);
    }

    protected LayerList createLayers()
    {
        Element el = Configuration.getElement("./LayerList");
        if (el != null)
        {
            Object o = BasicFactory.create(AVKey.LAYER_FACTORY, el);

            if (o instanceof LayerList)
                return (LayerList) o;

            else if (o instanceof Layer)
                return new LayerList(new Layer[] {(Layer) o});

            else if (o instanceof LayerList[])
            {
                LayerList[] lists = (LayerList[]) o;
                if (lists.length > 0)
                    return LayerList.collapseLists((LayerList[]) o);
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    public Globe getGlobe()
    {
        return this.globe;
    }

    /** {@inheritDoc} */
    public void setGlobe(Globe globe)
    {
        // don't raise an exception if globe == null. In that case, we are disassociating the model from any globe

        if (this.globe != null)
            this.globe.removePropertyChangeListener(this);
        if (globe != null)
            globe.addPropertyChangeListener(this);

        Globe old = this.globe;
        this.globe = globe;
        this.firePropertyChange(AVKey.GLOBE, old, this.globe);
    }

    /** {@inheritDoc} */
    public LayerList getLayers()
    {
        return this.layers;
    }

    /** {@inheritDoc} */
    public void setLayers(LayerList layers)
    {
        // don't raise an exception if layers == null. In that case, we are disassociating the model from any layer set

        if (this.layers != null)
            this.layers.removePropertyChangeListener(this);
        if (layers != null)
            layers.addPropertyChangeListener(this);

        LayerList old = this.layers;
        this.layers = layers;
        this.firePropertyChange(AVKey.LAYERS, old, this.layers);
    }
}
