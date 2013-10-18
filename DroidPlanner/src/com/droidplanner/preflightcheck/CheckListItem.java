package com.droidplanner.preflightcheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckListItem {
	private int categoryIndex;
	private String type;
	private String title;
	private String desc;
	private String sys_tag;
	private boolean editable;
	private boolean mandatory;
	private float min_val;
	private float nom_val;
	private float max_val;
	private int selectedIndex;
	private List<String> optionLists;

	public CheckListItem() {
		// TODO Auto-generated constructor stub
	}

	public CheckListItem(int mcategoryIndex, String mType, String mTitle,
			String mDescription, String mSysTag, String mMandatory,
			String mEditable) {
		this.setCategoryIndex(mcategoryIndex);
		this.setType(mType);
		this.setTitle(mTitle);
		this.setDesc(mDescription);
		this.setSys_tag(mSysTag);
		this.setMandatory(mMandatory);
		this.setEditable(mEditable);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
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
			this.editable = editable.equalsIgnoreCase("yes") ? true : false;
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
			this.mandatory = mandatory.equalsIgnoreCase("yes") ? true : false;
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

			for (int i = 0; i < optionLists.size(); i++)
				System.out.println("option : " + optionLists.get(i));

		}
	}

	public int getCategoryIndex() {
		return categoryIndex;
	}

	public void setCategoryIndex(int categoryIndex) {
		this.categoryIndex = categoryIndex;
	}

}
