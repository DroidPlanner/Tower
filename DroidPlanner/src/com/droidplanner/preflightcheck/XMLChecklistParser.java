package com.droidplanner.preflightcheck;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;

public class XMLChecklistParser {

	public interface OnXmlParserError {
		public void onError(XmlPullParser parser);
	}

	private OnXmlParserError errorListener;
	private XmlPullParser xpp;
	private List<String> categories;
	private List<CheckListItem> checkListItems;

	public void setOnXMLParserError(OnXmlParserError listener) {
		this.errorListener = listener;
	}

	public XMLChecklistParser() {
		categories = new ArrayList<String>();
		checkListItems = new ArrayList<CheckListItem>();
	}

	public void parse(String xmlStr) throws XmlPullParserException, IOException {

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		xpp = factory.newPullParser();
		xpp.setInput(new StringReader(xmlStr));
		do_parse(xpp);
	}

	public void parse(XmlResourceParser xmlpp) {
		xpp = xmlpp;
		do_parse(xpp);
	}

	public void next() {
		if (xpp != null)
			try {
				xpp.next();
			} catch (XmlPullParserException e) {
				if (errorListener != null)
					errorListener.onError(xpp);

				e.printStackTrace();
			} catch (IOException e) {
				if (errorListener != null)
					errorListener.onError(xpp);

				e.printStackTrace();
			}
	}

	public int getDepth() {
		if (xpp != null)
			return xpp.getDepth();
		return -1;
	}

	private void do_parse(XmlPullParser xpp) {

		int eventType = 0;
		try {
			eventType = xpp.getEventType();
		} catch (XmlPullParserException e) {
			if (errorListener != null)
				errorListener.onError(xpp);
			e.printStackTrace();
		}

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				process_StartDocument(xpp);
			} else if (eventType == XmlPullParser.END_DOCUMENT) {
				process_EndDocument(xpp);
			} else if (eventType == XmlPullParser.START_TAG) {
				process_StartTag(xpp);
			} else if (eventType == XmlPullParser.END_TAG) {
				process_EndTag(xpp);
			} else if (eventType == XmlPullParser.TEXT) {
				process_Text(xpp);
			}
			try {
				eventType = xpp.next();
			} catch (XmlPullParserException e) {
				if (errorListener != null)
					errorListener.onError(xpp);
				e.printStackTrace();
			} catch (IOException e) {
				if (errorListener != null)
					errorListener.onError(xpp);
				e.printStackTrace();
			}
		}
	}

	private void process_StartDocument(XmlPullParser xpp2) {
		// TODO Auto-generated method stub

	}

	private void process_EndDocument(XmlPullParser xpp2) {
		// TODO Auto-generated method stub

	}

	private void process_StartTag(XmlPullParser xpp2) {
		if (xpp2.getName().equalsIgnoreCase("category")) {
			process_category(xpp2);
		} else if (xpp2.getName().contains("_item")) {
			process_checkitems(xpp2);
		}
	}

	private void process_EndTag(XmlPullParser xpp2) {
		// TODO Auto-generated method stub

	}

	private void process_Text(XmlPullParser xpp2) {
		// TODO Auto-generated method stub

	}

	private void process_category(XmlPullParser xpp2) {
		String lbl = xpp2.getAttributeValue(null, "label");
		if (lbl == null)
			lbl = "Unknown";
		categories.add(lbl);
		System.out.println("Category - " + lbl);

	}

	private void process_checkitems(XmlPullParser xpp2) {
		
		String itemType = xpp2.getName();

		if (categories.size() == 0)
			return;

		if (itemType != null) {
			checkListItems.add(new CheckListItem(categories.size() - 1,
					xpp2.getName(), 
					xpp2.getAttributeValue(null, "title"),
					xpp2.getAttributeValue(null, "description"),
					xpp2.getAttributeValue(null, "system_tag"),
					xpp2.getAttributeValue(null, "editable"),
					xpp2.getAttributeValue(null, "mandatory")));
			
			CheckListItem checkListItem = checkListItems.get(checkListItems.size()-1);
			
			if (xpp2.getName().equalsIgnoreCase("check_item")) {
				addCheckItem(checkListItem, xpp2);
			} else if (xpp2.getName().equalsIgnoreCase("select_item")) {
				addSelectItem(checkListItem, xpp2);
			} else if (xpp2.getName().equalsIgnoreCase("toggle_item")) {
				addToggleItem(checkListItem, xpp2);
			} else if (xpp2.getName().equalsIgnoreCase("value_item")) {
				addValueItem(checkListItem, xpp2);
			} else if (xpp2.getName().equalsIgnoreCase("radio_item")) {
				addRadioItem(checkListItem, xpp2);
			}
		}
	}

	private void addCheckItem(CheckListItem checkListItem, XmlPullParser xpp2) {
		System.out.println("add Check Item");

	}

	private void addValueItem(CheckListItem checkListItem, XmlPullParser xpp2) {
		System.out.println("add Value Item");

		checkListItem.setMin_val(xpp2.getAttributeValue(null, "minimum_val"));
		checkListItem.setMax_val(xpp2.getAttributeValue(null, "maximum_val"));
		checkListItem.setNom_val(xpp2.getAttributeValue(null, "nominal_val"));

	}

	private void addToggleItem(CheckListItem checkListItem, XmlPullParser xpp2) {
		System.out.println("add Toggle Item");

	}

	private void addRadioItem(CheckListItem checkListItem, XmlPullParser xpp2) {
		System.out.println("add Radio Item");

		checkListItem.setSelectedIndex(xpp2.getAttributeValue(null,
				"selectindex"));
		checkListItem.setOptionLists(xpp.getAttributeValue(null, "optionlist"));

	}

	private void addSelectItem(CheckListItem checkListItem, XmlPullParser xpp2) {
		System.out.println("add Select Item");

		checkListItem.setSelectedIndex(xpp2.getAttributeValue(null,
				"selectindex"));
		checkListItem.setOptionLists(xpp.getAttributeValue(null, "optionlist"));
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<CheckListItem> getCheckListItems() {
		return checkListItems;
	}
}