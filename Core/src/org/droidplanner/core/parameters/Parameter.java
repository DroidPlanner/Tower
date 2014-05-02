package org.droidplanner.core.parameters;

import java.io.Serializable;
import java.text.DecimalFormat;

import com.MAVLink.Messages.ardupilotmega.msg_param_value;

public class Parameter implements Serializable {

	public String name;
	public double value;
	public int type;

	private final static DecimalFormat format = (DecimalFormat) DecimalFormat
			.getInstance();
	static {
		format.applyPattern("0.###");
	}

	public Parameter(String name, double value, int type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public Parameter(msg_param_value m_value) {
		this(m_value.getParam_Id(), m_value.param_value, m_value.param_type);
	}

	public Parameter(String name, Double value) {
		this(name, value, 0); // TODO Setting type to Zero may cause an error
	}

	public Parameter(String name) {
		this(name, 0, 0); // TODO Setting type to Zero may cause an error
	}

	public String getValue() {
		return format.format(value);
	}

	public static void checkParameterName(String name) throws Exception {
		if (name.equals("SYSID_SW_MREV")) {
			throw new Exception("ExludedName");
		} else if (name.contains("WP_TOTAL")) {
			throw new Exception("ExludedName");
		} else if (name.contains("CMD_TOTAL")) {
			throw new Exception("ExludedName");
		} else if (name.contains("FENCE_TOTAL")) {
			throw new Exception("ExludedName");
		} else if (name.contains("SYS_NUM_RESETS")) {
			throw new Exception("ExludedName");
		} else if (name.contains("ARSPD_OFFSET")) {
			throw new Exception("ExludedName");
		} else if (name.contains("GND_ABS_PRESS")) {
			throw new Exception("ExludedName");
		} else if (name.contains("GND_TEMP")) {
			throw new Exception("ExludedName");
		} else if (name.contains("CMD_INDEX")) {
			throw new Exception("ExludedName");
		} else if (name.contains("LOG_LASTFILE")) {
			throw new Exception("ExludedName");
		} else if (name.contains("FORMAT_VERSION")) {
			throw new Exception("ExludedName");
		} else {
			return;
		}
	}

	public static DecimalFormat getFormat() {
		return format;
	}
}
