package org.droidplanner.android.view.checklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.droidplanner.android.view.checklist.xml.ListXmlData;

public class CheckListItem extends ListXmlData {
	private int categoryIndex;
	private String title;
	private String desc;
	private String unit;
	private String on_label;
	private String off_label;
	private String sys_tag;
	private String value;
	private boolean editable;
	private boolean mandatory;
	private int selectedIndex;
	private float min_val;
	private float nom_val;
	private float max_val;
	private double sys_value;
	private boolean verified;
	private boolean sys_activated;
	private List<String> optionLists;

	public CheckListItem(String mTagName) {
		super(mTagName);
	}

	public CheckListItem(int mcategoryIndex, String mTagName, String mTitle, String mDescription,
			String mSysTag, String mMandatory, String mEditable) {
		super(mTagName);
		this.setCategoryIndex(mcategoryIndex);
		this.setTitle(mTitle);
		this.setDesc(mDescription);
		this.setSys_tag(mSysTag);
		this.setMandatory(mMandatory);
		this.setEditable(mEditable);
	}

	public String getTitle() {
		if (title == null)
			return "No Title";
		if (isMandatory())
			return "* " + title;
		else
			return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		if (desc == null)
			return "";
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getSys_tag() {
		return sys_tag;
	}

	public void setSys_tag(String sys_tag) {
		this.sys_tag = sys_tag;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setEditable(String editable) {
		if (editable != null) {
			this.editable = editable.equalsIgnoreCase("yes");
		}
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public void setMandatory(String mandatory) {
		if (mandatory != null) {
			this.mandatory = mandatory.equalsIgnoreCase("yes");
		}
	}

	public float getMin_val() {
		return min_val;
	}

	public void setMin_val(float min_val) {
		this.min_val = min_val;
	}

	public void setMin_val(String min_val) {
		if (min_val != null) {
			try {
				this.min_val = Float.parseFloat(min_val);
			} catch (NumberFormatException e) {
				this.min_val = 0;
				e.printStackTrace();
			}

		}
	}

	public float getNom_val() {
		return nom_val;
	}

	public void setNom_val(float nom_val) {
		this.nom_val = nom_val;
	}

	public void setNom_val(String nom_val) {
		if (nom_val != null) {
			try {
				this.nom_val = Float.parseFloat(nom_val);
			} catch (NumberFormatException e) {
				this.nom_val = 0;
				e.printStackTrace();
			}

		}
	}

	public float getMax_val() {
		return max_val;
	}

	public void setMax_val(float max_val) {
		this.max_val = max_val;
	}

	public void setMax_val(String max_val) {
		if (max_val != null) {
			try {
				this.max_val = Float.parseFloat(max_val);
			} catch (NumberFormatException e) {
				this.max_val = 0;
				e.printStackTrace();
			}

		}
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public void setSelectedIndex(String selectedIndex) {
		if (selectedIndex != null) {
			try {
				this.selectedIndex = Integer.parseInt(selectedIndex);
			} catch (NumberFormatException e) {
				this.selectedIndex = -1;
				e.printStackTrace();
			}
		}
	}

	public List<String> getOptionLists() {
		return optionLists;
	}

	public void setOptionLists(String optionListStr) {
		this.optionLists = null;

		if (optionListStr != null) {
			this.optionLists = new ArrayList<String>(
					Arrays.asList(optionListStr.split("\\s*,\\s*")));

			for (String optionList : optionLists)
				System.out.println("option : " + optionList);

		}
	}

	public int getCategoryIndex() {
		return categoryIndex;
	}

	public void setCategoryIndex(int categoryIndex) {
		this.categoryIndex = categoryIndex;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		if (unit != null) {
			this.unit = unit;
		} else {
			this.unit = "";
		}
	}

	public double getSys_value() {
		return sys_value;
	}

	public void setSys_value(double d) {
		this.sys_value = d;

		try {
			this.value = String.valueOf(this.sys_value);
		} catch (Exception e) {
			this.value = "0.0";
			e.printStackTrace();
		}
	}

	public boolean isSys_activated() {
		return sys_activated;
	}

	public void setSys_activated(boolean sys_activated) {
		this.sys_activated = sys_activated;
	}

	public String getOn_label() {
		if (on_label == null)
			return "";
		return on_label;
	}

	public void setOn_label(String on_label) {
		this.on_label = on_label;
	}

	public String getOff_label() {
		if (off_label == null)
			return "";
		return off_label;
	}

	public void setOff_label(String off_label) {
		this.off_label = off_label;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getValue() {
		if (value == null)
			return "";
		return value;
	}

	public float getFloatValue() {
		float fValue = (float) 0.0;
		if (value != null) {
			try {
				fValue = Float.parseFloat(value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return fValue;

	}

	public void setValue(String value) {
		this.value = value;
	}

}
