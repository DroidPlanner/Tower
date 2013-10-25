package com.droidplanner.dialogs.checklist;

import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListAdapter;
import com.droidplanner.checklist.CheckListAdapter.OnCheckListItemUpdateListener;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.checklist.CheckListXmlParser;
import com.droidplanner.checklist.xml.ListXmlParser.OnXmlParserError;
import com.droidplanner.drone.Drone;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PreflightDialog implements DialogInterface.OnClickListener,
		OnXmlParserError, OnCheckListItemUpdateListener {

	private Context context;
	private View view;
	private Drone drone;
	private List<String> listDataHeader;
	private List<CheckListItem> checkItemList;
	private HashMap<String, List<CheckListItem>> listDataChild;
	private CheckListAdapter listAdapter;
	private ExpandableListView expListView;
	private AlertDialog dialog;

	public PreflightDialog() {
		// TODO Auto-generated constructor stub
	}

	// public void build(Drone mdrone, Context mcontext, boolean mpreflight) {
	public void build(Context mcontext, Drone mdrone, boolean mpreflight) {
		context = mcontext;
		drone = mdrone;
		// TODO Read System checklist here
		CheckListXmlParser xml = new CheckListXmlParser(mcontext,
				R.xml.checklist_default);
		xml.setOnXMLParserError(this);
		listDataHeader = xml.getCategories();
		checkItemList = xml.getCheckListItems();

		dialog = buildDialog(mpreflight);
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
		view = inflater.inflate(R.layout.layout_checklist, null);
		// get the listview
		expListView = (ExpandableListView) view.findViewById(R.id.expListView);

		// preparing list data
		prepareListData();

		listAdapter = new CheckListAdapter(drone, inflater, listDataHeader,
				listDataChild);
		listAdapter.setHeaderLayout(R.layout.list_group_header);
		listAdapter.setOnCheckListItemUpdateListener(this);
		// setting list adapter

		expListView.post(new Runnable() {

			@Override
			public void run() {
				dialog.getWindow()
						.clearFlags(
								WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
										| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				dialog.getWindow().setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			}
		});
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
	public void onRadioGroupUpdate(CheckListItem checkListItem, int checkId) {
		Toast.makeText(
				context,
				checkListItem.getTitle() + " : "
						+ checkListItem.getOptionLists().get(checkId),
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onSelectUpdate(CheckListItem checkListItem, int selectId) {
		Toast.makeText(
				context,
				checkListItem.getTitle() + " : "
						+ checkListItem.getOptionLists().get(selectId),
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onCheckBoxUpdate(CheckListItem checkListItem, boolean isChecked) {
		Toast.makeText(
				context,
				checkListItem.getTitle() + " : " + checkListItem.getTitle()
						+ (isChecked ? " checked" : " unchecked"),
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onSwitchUpdate(CheckListItem checkListItem, boolean isSwitched) {
		Toast.makeText(
				context,
				checkListItem.getTitle() + " : " + checkListItem.getTitle()
						+ (isSwitched ? " switched ON" : " switched OFF"),
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onToggleUpdate(CheckListItem checkListItem, boolean isToggled) {
		Toast.makeText(
				context,
				checkListItem.getTitle() + " : " + checkListItem.getTitle()
						+ (isToggled ? " toggled ON" : " toggled OFF"),
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onValueUpdate(CheckListItem checkListItem, String newValue) {
		// TODO Auto-generated method stub

	}

}
