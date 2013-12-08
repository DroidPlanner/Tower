package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.checklist.CheckListAdapter;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.checklist.CheckListSysLink;
import com.droidplanner.checklist.CheckListXmlParser;
import com.droidplanner.checklist.CheckListAdapter.OnCheckListItemUpdateListener;
import com.droidplanner.checklist.xml.ListXmlParser.OnXmlParserError;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.InfoListner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class CheckListFragment extends Fragment implements OnXmlParserError,
		OnCheckListItemUpdateListener, InfoListner {

	private Context context;
	private LayoutInflater inflater;
	private View view;
	private Drone drone;
	private String listTitle;
	private List<String> listDataHeader;
	private List<CheckListItem> checkItemList;
	private HashMap<String, List<CheckListItem>> listDataChild;
	private CheckListAdapter listAdapter;
	private ExpandableListView expListView;
	private CheckListSysLink sysLink;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.inflater = inflater;
		this.view = inflater.inflate(R.layout.fragment_checklist, container,
				false);
		
		expListView = (ExpandableListView) view.findViewById(R.id.expListView);
		return view;
	}

	private void setupListView() {

		// preparing list data
		prepareListData();

		listAdapter = new CheckListAdapter(drone, inflater, listDataHeader,
				listDataChild);
		listAdapter.setHeaderLayout(R.layout.list_group_header);
		listAdapter.setOnCheckListItemUpdateListener(this);
		// setting list adapter

		/*
		 * expListView.post(new Runnable() {
		 * 
		 * @Override public void run() { dialog.getgetWindow().clearFlags(
		 * WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
		 * WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		 * dialog.getWindow().setSoftInputMode(
		 * WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); } });
		 */
		expListView.setAdapter(listAdapter);

		listViewAutoExpand(true, true);
	}

	private void listViewAutoExpand(boolean autoExpand, boolean autoCollapse) {
		boolean allVerified;
		for (int h = 0; h < listDataHeader.size(); h++) {
			allVerified = listAdapter.areAllVerified(h);
			if (!allVerified && autoExpand)
				expListView.expandGroup(h);
			else if (allVerified && autoCollapse)
				expListView.collapseGroup(h);
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        context = activity;
        drone = ((SuperActivity) activity).drone;	
		sysLink = new CheckListSysLink(drone);
		
		// If external file is not found, load the default
		CheckListXmlParser xml = new CheckListXmlParser("checklist_ext.xml",context,
				R.xml.checklist_default);

		xml.setOnXMLParserError(this);
		listDataHeader = xml.getCategories();
		checkItemList = xml.getCheckListItems();
		listTitle = xml.getCheckListTitle();
		setupListView();
	 }

	@Override
	public void onDestroy() {
		super.onDestroy();
	
	}

	public void update() {
		listAdapter.notifyDataSetChanged();

	}
	
	@Override
	public void onInfoUpdate() {
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

	@Override
	public void onRowItemChanged(CheckListItem checkListItem, String mSysTag,
			boolean isChecked) {
		sysLink.setSystemData(checkListItem);
		listAdapter.notifyDataSetChanged();
		listViewAutoExpand(false, true);

	}

	@Override
	public void onRowItemGetData(CheckListItem checkListItem, String mSysTag) {
		sysLink.getSystemData(checkListItem, mSysTag);

	}

	@Override
	public void onError(XmlPullParser parser) {
		// TODO Auto-generated method stub

	}

}
