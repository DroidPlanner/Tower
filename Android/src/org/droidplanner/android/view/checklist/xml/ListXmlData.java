package org.droidplanner.android.view.checklist.xml;

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
		return depth;
	}
}
