package org.droidplanner.android.view.checklist;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.view.checklist.xml.ListXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

public class CheckListXmlParser extends ListXmlParser {

	private List<String> categories;
	private List<CheckListItem> checkListItems;
	private String checkListTitle;
	private String checkListType;
	private String checkListVersion;

	@Override
	public void setOnXMLParserError(OnXmlParserError listener) {
		errorListener = listener;
	}

	public CheckListXmlParser() {
		categories = new ArrayList<String>();
		checkListItems = new ArrayList<CheckListItem>();
	}

	public CheckListXmlParser(Context context, int resourceId) {
		categories = new ArrayList<String>();
		checkListItems = new ArrayList<CheckListItem>();
		getListItemsFromResource(context, resourceId);
	}

	public CheckListXmlParser(String ioFile, Context context, int resourceId) {
		categories = new ArrayList<String>();
		checkListItems = new ArrayList<CheckListItem>();
		try {
			getListItemsFromFile(ioFile);
		} catch (FileNotFoundException e) {
			getListItemsFromResource(context, resourceId);
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public CheckListXmlParser(String ioFile) {
		categories = new ArrayList<String>();
		checkListItems = new ArrayList<CheckListItem>();
		try {
			getListItemsFromFile(ioFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void process_category(XmlPullParser xpp) {
		String lbl = xpp.getAttributeValue(null, "label");
		if (lbl == null)
			lbl = "Unknown";
		categories.add(lbl);
		System.out.println("Category - " + lbl);

	}

	private void process_checkitems(XmlPullParser xpp) {

		String itemType = xpp.getName();

		if (categories.size() == 0)
			return;

		if (itemType != null) {
			checkListItems.add(new CheckListItem(xpp.getName()));
			CheckListItem checkListItem = checkListItems.get(checkListItems.size() - 1);
			checkListItem.setDepth(xpp.getDepth());
			checkListItem.setCategoryIndex(categories.size() - 1);
			checkListItem.setTitle(xpp.getAttributeValue(null, "title"));
			checkListItem.setDesc(xpp.getAttributeValue(null, "description"));
			checkListItem.setUnit(xpp.getAttributeValue(null, "unit"));
			checkListItem.setOn_label(xpp.getAttributeValue(null, "on_label"));
			checkListItem.setOff_label(xpp.getAttributeValue(null, "off_label"));
			checkListItem.setSys_tag(xpp.getAttributeValue(null, "system_tag"));
			checkListItem.setEditable(xpp.getAttributeValue(null, "editable"));
			checkListItem.setMandatory(xpp.getAttributeValue(null, "mandatory"));
			checkListItem.setNom_val(xpp.getAttributeValue(null, "nominal_val"));
			checkListItem.setMin_val(xpp.getAttributeValue(null, "minimum_val"));
			checkListItem.setMax_val(xpp.getAttributeValue(null, "maximum_val"));
			checkListItem.setValue(xpp.getAttributeValue(null, "value"));
			checkListItem.setSelectedIndex(xpp.getAttributeValue(null, "selectindex"));
			checkListItem.setOptionLists(xpp.getAttributeValue(null, "optionlist"));
		}
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<CheckListItem> getCheckListItems() {
		return checkListItems;
	}

	@Override
	public void process_StartDocument() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process_EndDocument() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process_StartTag(XmlPullParser xpp) {
		if (xpp.getName().equalsIgnoreCase("category")) {
			process_category(xpp);
		} else if (xpp.getName().contains("_item")) {
			process_checkitems(xpp);
		} else if (xpp.getDepth() == 1) {
			this.checkListTitle = xpp.getAttributeValue(null, "title");
			this.checkListType = xpp.getAttributeValue(null, "type");
			this.checkListVersion = xpp.getAttributeValue(null, "version");
		}
	}

	@Override
	public void process_EndTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process_Text() {
		// TODO Auto-generated method stub

	}

	public String getCheckListTitle() {
		return checkListTitle;
	}

	public String getCheckListType() {
		return checkListType;
	}

	public String getCheckListVersion() {
		return checkListVersion;
	}
}
