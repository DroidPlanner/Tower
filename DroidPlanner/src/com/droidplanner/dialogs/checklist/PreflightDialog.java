package com.droidplanner.dialogs.checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListAdapter;
import com.droidplanner.checklist.CheckListAdapter.OnCheckListItemUpdateListener;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.checklist.CheckListSysLink;
import com.droidplanner.checklist.CheckListXmlParser;
import com.droidplanner.checklist.xml.ListXmlParser.OnXmlParserError;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.InfoListner;

public class PreflightDialog implements DialogInterface.OnClickListener,
		OnXmlParserError, OnCheckListItemUpdateListener, InfoListner {

	private Context context;
	private View view;
	private Drone drone;
	private String listTitle;
	private List<String> listDataHeader;
	private List<CheckListItem> checkItemList;
	private HashMap<String, List<CheckListItem>> listDataChild;
	private CheckListAdapter listAdapter;
	private ExpandableListView expListView;
	private AlertDialog dialog;
	private CheckListSysLink sysLink;

	public PreflightDialog() {
		// TODO Auto-generated constructor stub
	}

	// public void build(Drone mdrone, Context mcontext, boolean mpreflight) {
	public void build(Context mcontext, Drone mdrone, boolean mpreflight) {
		context = mcontext;
		drone = mdrone;
		sysLink = new CheckListSysLink(drone);
		// If external file is not found, load the default
		CheckListXmlParser xml = new CheckListXmlParser("checklist_ext.xml",mcontext,
				R.xml.checklist_default);

		xml.setOnXMLParserError(this);
		listDataHeader = xml.getCategories();
		checkItemList = xml.getCheckListItems();
		listTitle = xml.getCheckListTitle();
		dialog = buildDialog(mpreflight);
		drone.addInfoListener(this);
		dialog.show();
	}

	private AlertDialog buildDialog(boolean mpreflight) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(listTitle);
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
		
		listViewAutoExpand(true,true);
		

		return view;
	}

	private void listViewAutoExpand(boolean autoExpand, boolean autoCollapse) {
		boolean allVerified;

		try {
			for(int h =0; h<listDataHeader.size();h++){
				allVerified = listAdapter.areAllVerified(h);
				if(!allVerified&&autoExpand)
						expListView.expandGroup(h);
				else if(allVerified&&autoCollapse)
					expListView.collapseGroup(h);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	public void onRowItemChanged(CheckListItem checkListItem, String mSysTag,
			boolean isChecked) {
		sysLink.setSystemData(checkListItem);
		listAdapter.notifyDataSetChanged();
		listViewAutoExpand(false,true);
	}

	@Override
	public void onRowItemGetData(CheckListItem checkListItem, String mSysTag) {
		sysLink.getSystemData(checkListItem, mSysTag);
	}

	@Override
	public void onInfoUpdate() {
		if(!dialog.isShowing())
			return;
		if(checkItemList==null)
			return;
		if(checkItemList.size()<=0)
			return;
		
		for(int i=0;i< checkItemList.size();i++){
			CheckListItem checkListItem = checkItemList.get(i);
			if(checkListItem!=null && checkListItem.getSys_tag()!=null){
//			Log.d("CHKLST", checkListItem.getSys_tag());
 				sysLink.getSystemData(checkListItem, checkListItem.getSys_tag());
			}
		}
		listAdapter.notifyDataSetChanged();
//		listViewAutoExpand(true,true);

	}


}
