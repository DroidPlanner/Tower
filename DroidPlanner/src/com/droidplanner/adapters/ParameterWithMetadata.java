package com.droidplanner.adapters;

import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

import java.io.Serializable;

/**
* User: rgayle
* Date: 2013-12-09
* Time: 1:32 AM
*/
public class ParameterWithMetadata implements Serializable {
    private Parameter parameter;
    private ParameterMetadata metadata;

    public ParameterWithMetadata(Parameter parameter, ParameterMetadata metadata) {
        this.parameter = parameter;
        this.metadata = metadata;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public ParameterMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ParameterMetadata metadata) {
        this.metadata = metadata;
    }
}
