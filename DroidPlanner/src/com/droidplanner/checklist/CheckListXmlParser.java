package com.droidplanner.checklist;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.droidplanner.checklist.xml.ListXmlParser;

import android.content.Context;

public class CheckListXmlParser extends ListXmlParser {

	private List<String> categories;
	private List<CheckListItem> checkListItems;

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

	public CheckListXmlParser(String ioFile) {
		categories = new ArrayList<String>();
		checkListItems = new ArrayList<CheckListItem>();
		getListItemsFromFile(ioFile);
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
			CheckListItem checkListItem = checkListItems.get(checkListItems
					.size() - 1);

			checkListItem.setCategoryIndex(categories.size() - 1);
			checkListItem.setTitle(xpp.getAttributeValue(null, "title"));
			checkListItem.setDesc(xpp.getAttributeValue(null, "description"));
			checkListItem.setUnit(xpp.getAttributeValue(null, "unit"));
			checkListItem.setUnit(xpp.getAttributeValue(null, "on_label"));
			checkListItem.setUnit(xpp.getAttributeValue(null, "off_label"));
			checkListItem.setSys_tag(xpp.getAttributeValue(null, "system_tag"));
			checkListItem.setEditable(xpp.getAttributeValue(null, "editable"));
			checkListItem.setMandatory(xpp.getAttributeValue(null, "mandatory"));
			checkListItem.setMin_val(xpp.getAttributeValue(null, "minimum_val"));
			checkListItem.setMax_val(xpp.getAttributeValue(null, "maximum_val"));
			checkListItem.setNom_val(xpp.getAttributeValue(null, "nominal_val"));
			checkListItem.setSelectedIndex(xpp.getAttributeValue(null,"selectindex"));
			checkListItem.setOptionLists(xpp.getAttributeValue(null,"optionlist"));
		}
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<CheckListItem> getCheckListItems() {
		return checkListItems;
	}

	@Override
	public void process_StartDocument(XmlPullParser xpp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process_EndDocument(XmlPullParser xpp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process_StartTag(XmlPullParser xpp) {
		if (xpp.getName().equalsIgnoreCase("category")) {
			process_category(xpp);
		} else if (xpp.getName().contains("_item")) {
			process_checkitems(xpp);
		}
	}

	@Override
	public void process_EndTag(XmlPullParser xpp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process_Text(XmlPullParser xpp) {
		// TODO Auto-generated method stub

	}

}
