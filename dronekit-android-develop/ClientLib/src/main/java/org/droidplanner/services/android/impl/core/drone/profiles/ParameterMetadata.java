package org.droidplanner.services.android.impl.core.drone.profiles;

import java.io.Serializable;

public class ParameterMetadata implements Serializable {

	private String name;
	private String displayName;
	private String description;

	private String units;
	private String range;
	private String values;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}


}
