package com.droidplanner.dialogs.checklist;

import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.View;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.preflightcheck.CheckListItem;
import com.droidplanner.preflightcheck.XMLChecklistParser;
import com.droidplanner.widgets.ChecklistAdapter.CheckListAdapter;
import com.droidplanner.widgets.ChecklistAdapter.CheckListAdapter.OnCheckListItemUpdateListener;

import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class PreflightDialog implements DialogInterface.OnClickListener,
		com.droidplanner.preflightcheck.XMLChecklistParser.OnXmlParserError, OnCheckListItemUpdateListener {

	private Context context;
	private View view;
	private Drone drone;
	private List<String> listDataHeader;
	private List<CheckListItem> checkItemList;
	private HashMap<String, List<CheckListItem>> listDataChild;
	private CheckListAdapter listAdapter;
	private ExpandableListView expListView;

	public PreflightDialog() {
		// TODO Auto-generated constructor stub
	}

//	public void build(Drone mdrone, Context mcontext, boolean mpreflight) {
	public void build(Context mcontext, Drone mdrone, boolean mpreflight) {
		context = mcontext;
		drone = mdrone;
		// TODO Read System checklist here
		XMLChecklistParser xml = new XMLChecklistParser();
		xml.setOnXMLParserError(this);

		XmlResourceParser is = context.getResources().getXml(
				R.xml.checklist_default);
		xml.parse(is);
		listDataHeader = xml.getCategories();
		checkItemList = xml.getCheckListItems();

		AlertDialog dialog = buildDialog(mpreflight);
		dialog.show();
	}

	private AlertDialog buildDialog(boolean mpreflight) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Pre-Flight Check");
		builder.setView(buildView());
		builder.setPositiveButton("Ok", this);
		if (mpreflight) {
			builder.setNegativeButton("Cancel", this);
		}
		AlertDialog dialog = builder.create();
		return dialog;
	}

	protected View buildView() {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.dialog_preflight, null);
		// get the listview
		expListView = (ExpandableListView) view.findViewById(R.id.lvExp);

		// preparing list data
		prepareListData();

		listAdapter = new CheckListAdapter(inflater, listDataHeader,
				listDataChild);
		listAdapter.setOnCheckListItemUpdateListener(this);
		// setting list adapter
		expListView.setAdapter(listAdapter);
		expListView.expandGroup(0);
		expListView.expandGroup(1);

		return view;
	}

	private void prepareListData() {
		listDataChild = new HashMap<String, List<CheckListItem>>();
		List<CheckListItem> cli;

		for (int h = 0; h < listDataHeader.size(); h++) {
			cli = new ArrayList<CheckListItem>();
			for (int i = 0; i < checkItemList.size(); i++) {
				CheckListItem c = checkItemList.get(i);
				if (c.getCategoryIndex() == h)
					cli.add(c);
			}
			listDataChild.put(listDataHeader.get(h), cli);
		}
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {

	}

	@Override
	public void onError(XmlPullParser parser) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRadioGroupUpdate(CheckListItem checkListItem,
			RadioGroup group, int checkId) {
		Toast.makeText(context, checkListItem.getTitle(), Toast.LENGTH_SHORT).show();
		
	}


}
