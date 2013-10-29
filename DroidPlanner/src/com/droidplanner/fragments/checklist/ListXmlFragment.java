package com.droidplanner.fragments.checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.droidplanner.R;
import com.droidplanner.checklist.CheckListAdapter;
import com.droidplanner.checklist.CheckListAdapter.OnCheckListItemUpdateListener;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.checklist.CheckListXmlParser;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ExpandableListView;

public class ListXmlFragment extends DialogFragment implements
		OnCheckListItemUpdateListener {
	private List<String> listDataHeader;
	private List<CheckListItem> checkItemList;
	private HashMap<String, List<CheckListItem>> listDataChild;
	private CheckListAdapter listAdapter;

	public ListXmlFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_checklist, container);
		ExpandableListView expListView = (ExpandableListView) view
				.findViewById(R.id.expListView);

		getXmlListItems();
		setDialogOptions();
		
//		listAdapter = new CheckListAdapter(inflater, listDataHeader,
//				listDataChild);
//		listAdapter.setOnCheckListItemUpdateListener(this);
//		expListView.setAdapter(listAdapter);
		return view;
	}

	private void getXmlListItems() {
		CheckListXmlParser xml = new CheckListXmlParser();
		xml.getListItemsFromResource(getActivity(), R.xml.checklist_default);
		listDataHeader = xml.getCategories();
		checkItemList = xml.getCheckListItems();

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

	private void setListView(ExpandableListView expListView) {
		// setting list adapter

	}

	private void setDialogOptions() {
        
		getDialog().setTitle("Pre-Flight Checklist");
		getDialog().getWindow().setSoftInputMode(
                LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		// getDialog().getWindow().setBackgroundDrawableResource(R.drawable.round_dialog);
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo);
	}

	@Override
	public void onRowItemChanged(CheckListItem checkListItem, String mSysTag,
			boolean isChecked) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRowItemGetData(CheckListItem checkListItem, String mSysTag) {
		// TODO Auto-generated method stub
		
	}
}
