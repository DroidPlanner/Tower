package org.droidplanner.android.widgets.checklist.xml;

public class ListXmlData implements ListXmlData_Interface {
	private String tagName;
	private int depth;

	public ListXmlData(String mTagName) {
		this.tagName = mTagName;
	}

	public void setDepth(int mDepth) {
		this.depth = mDepth;
	}

	@Override
	public String getTagName() {
		return tagName;
	}

	@Override
	public int getDepth() {
		// TODO Auto-generated method stub
		return depth;
	}
}
