package org.droidplanner.core.parameters;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParameterMetadata implements Serializable {
	public static final int RANGE_LOW = 0;
	public static final int RANGE_HIGH = 1;

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

	public boolean hasInfo() {
		return (description != null && !description.isEmpty())
				|| (values != null && !values.isEmpty());
	}

	public double[] parseRange() throws ParseException {
		final DecimalFormat format = Parameter.getFormat();

		final String[] parts = this.range.split(" ");
		if (parts.length != 2) {
			throw new IllegalArgumentException();
		}

		final double[] outRange = new double[2];
		outRange[RANGE_LOW] = format.parse(parts[RANGE_LOW]).doubleValue();
		outRange[RANGE_HIGH] = format.parse(parts[RANGE_HIGH]).doubleValue();

		return outRange;
	}

	public Map<Double, String> parseValues() throws ParseException {
		final DecimalFormat format = Parameter.getFormat();

		final Map<Double, String> outValues = new LinkedHashMap<Double, String>();
		if (values != null) {
			final String[] tparts = this.values.split(",");
			for (String tpart : tparts) {
				final String[] parts = tpart.split(":");
				if (parts.length != 2)
					throw new IllegalArgumentException();
				outValues.put(format.parse(parts[0].trim()).doubleValue(), parts[1].trim());
			}
		}
		return outValues;
	}
}
