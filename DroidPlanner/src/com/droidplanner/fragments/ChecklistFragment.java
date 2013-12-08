package com.droidplanner.fragments;

import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.checklist.CheckListItem;
import com.droidplanner.checklist.CheckListXmlParser;
import com.droidplanner.checklist.xml.ListXmlParser.OnXmlParserError;
import com.droidplanner.drone.Drone;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class ChecklistFragment extends Fragment implements OnXmlParserError{
	private Context context;
	private Drone drone;
	private View view;
	private ExpandableListView expListView;
	private List<String> listDataHeader;
	private List<CheckListItem> checklistItems;

	//Load checklist from file
	private void loadXMLChecklist() {
		if(context==null)
			return;
		
		CheckListXmlParser xml = new CheckListXmlParser("checklist_ext.xml",context,
				R.xml.checklist_default);

		xml.setOnXMLParserError(this);
		listDataHeader = xml.getCategories();
		checklistItems = xml.getCheckListItems();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_checklist, null);
		expListView = (ExpandableListView) view.findViewById(R.id.expListView);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        this.context = activity;
        this.drone = ((SuperActivity) activity).drone;
        loadXMLChecklist();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public ChecklistFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onError(XmlPullParser parser) {
		// TODO Auto-generated method stub
		
	}

}
