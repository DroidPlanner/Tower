package com.droidplanner.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.droidplanner.R;

public class EditorToolsFragment extends Fragment implements OnClickListener {

	public enum EditorTools {
		MARKER, DRAW, POLY, TRASH
	}

	public interface OnEditorToolSelected {
		public void editorToolChanged(EditorTools tools);
	}

	private OnEditorToolSelected listner;
	private RadioButton buttonDraw;
	private RadioButton buttonMarker;
	private RadioButton buttonPoly;
	private RadioButton buttonTrash;
	private EditorTools tool = EditorTools.MARKER;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_editor_control,container, false);

		buttonDraw 		= (RadioButton) view.findViewById(R.id.editor_tools_draw);
		buttonMarker 	= (RadioButton) view.findViewById(R.id.editor_tools_marker);
		buttonPoly 		= (RadioButton) view.findViewById(R.id.editor_tools_poly);
		buttonTrash 	= (RadioButton) view.findViewById(R.id.editor_tools_trash);

		buttonDraw.setOnClickListener(this);
		buttonMarker.setOnClickListener(this);
		buttonPoly.setOnClickListener(this);
		buttonTrash.setOnClickListener(this);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listner = (OnEditorToolSelected) activity;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.editor_tools_marker:
				tool = EditorTools.MARKER;
				buttonDraw.setChecked(false);
				buttonPoly.setChecked(false);
				buttonTrash.setChecked(false);
				break;
			case R.id.editor_tools_draw:
				tool = EditorTools.DRAW;
				buttonMarker.setChecked(false);
				buttonPoly.setChecked(false);
				buttonTrash.setChecked(false);
				break;
			case R.id.editor_tools_poly:
				tool = EditorTools.POLY;

				buttonMarker.setChecked(false);
				buttonDraw.setChecked(false);
				buttonTrash.setChecked(false);
				break;
			case R.id.editor_tools_trash:
				tool = EditorTools.TRASH;
				buttonMarker.setChecked(false);
				buttonDraw.setChecked(false);
				buttonPoly.setChecked(false);
				break;
		}
		listner.editorToolChanged(getTool());
	}

	public EditorTools getTool() {
		return tool;
	}

	public void setTool(EditorTools marker) {
		RadioButton selected = null;

		switch (marker) {
			case DRAW:
				selected = buttonDraw;
				break;
			case MARKER:
				selected = buttonMarker;
				break;
			case POLY:
				selected = buttonPoly;
				break;
			case TRASH:
				selected = buttonTrash;
				break;
		}
		selected.setChecked(true);
		onClick(selected);

	}

}
