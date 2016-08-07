package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.view.checklist.CheckListAdapter;
import org.droidplanner.android.view.checklist.CheckListAdapter.OnCheckListItemUpdateListener;
import org.droidplanner.android.view.checklist.CheckListItem;
import org.droidplanner.android.view.checklist.CheckListSysLink;
import org.droidplanner.android.view.checklist.CheckListXmlParser;
import org.droidplanner.android.view.checklist.xml.ListXmlParser.OnXmlParserError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChecklistFragment extends ApiListenerFragment implements
	OnXmlParserError,
		OnCheckListItemUpdateListener {

    private final static IntentFilter intentFilter = new IntentFilter();
	static {
		intentFilter.addAction(AttributeEvent.BATTERY_UPDATED);
		intentFilter.addAction(AttributeEvent.GPS_COUNT);
		intentFilter.addAction(AttributeEvent.GPS_FIX);
		intentFilter.addAction(AttributeEvent.GPS_POSITION);
		intentFilter.addAction(AttributeEvent.STATE_CONNECTED);
		intentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
		intentFilter.addAction(AttributeEvent.STATE_UPDATED);
		intentFilter.addAction(AttributeEvent.STATE_ARMING);
	}

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
			onInfoUpdate();
        }
    };

	private Context context;
	private ExpandableListView expListView;
	private List<String> listDataHeader;
	private List<CheckListItem> checklistItems;
	private HashMap<String, List<CheckListItem>> listDataChild;
	private CheckListAdapter listAdapter;
	private CheckListSysLink sysLink;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_checklist, container, false);
		expListView = (ExpandableListView) view.findViewById(R.id.expListView);

		createListAdapter(inflater);
		expListView.setAdapter(listAdapter);

		listViewAutoExpand(true, true);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.context = activity;
		loadXMLChecklist();
		prepareListData();
	}

	@Override
	public void onDetach() {
		sysLink = null;
		listAdapter = null;
		listDataHeader = null;
		listDataChild = null;
		checklistItems = null;
		super.onDetach();
	}

    @Override
    public void onApiConnected(){
        sysLink = new CheckListSysLink(getActivity().getApplicationContext(), getDrone());
        getBroadcastManager().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onApiDisconnected(){
        getBroadcastManager().unregisterReceiver(broadcastReceiver);
    }

	public void onInfoUpdate() {
        if(checklistItems != null && !checklistItems.isEmpty()) {
            for (CheckListItem item : checklistItems) {
                if (item.getSys_tag() != null) {
                    sysLink.getSystemData(item, item.getSys_tag());
                }
            }
        }
		if (listAdapter != null)
			listAdapter.notifyDataSetChanged();
	}

	// Load checklist from file
	private void loadXMLChecklist() {
		CheckListXmlParser xml = new CheckListXmlParser("checklist_ext.xml", context,
				R.xml.checklist_default);

		xml.setOnXMLParserError(this);
		listDataHeader = xml.getCategories();
		checklistItems = xml.getCheckListItems();
	}

	// create hash list
	private void prepareListData() {
		listDataChild = new HashMap<String, List<CheckListItem>>();
		List<CheckListItem> cli;

		for (int h = 0; h < listDataHeader.size(); h++) {
			cli = new ArrayList<CheckListItem>();
			for (CheckListItem c : checklistItems) {
				if (c.getCategoryIndex() == h)
					cli.add(c);
			}
			listDataChild.put(listDataHeader.get(h), cli);
		}
	}

	// create listAdapter
	private void createListAdapter(LayoutInflater layoutInflater) {
		listAdapter = new CheckListAdapter(layoutInflater, listDataHeader, listDataChild);

		listAdapter.setHeaderLayout(R.layout.list_group_header);
		listAdapter.setOnCheckListItemUpdateListener(this);
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

	@Override
	public void onError() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRowItemChanged(CheckListItem checkListItem) {
        if(sysLink != null) {
            sysLink.setSystemData(checkListItem);
            listAdapter.notifyDataSetChanged();
            listViewAutoExpand(false, true);
        }
	}

	@Override
	public void onRowItemGetData(CheckListItem checkListItem, String mSysTag) {
        if(sysLink != null) {
            sysLink.getSystemData(checkListItem, mSysTag);
        }
	}
}
