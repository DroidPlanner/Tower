package org.droidplanner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;

import org.droidplanner.R;

public class EditorToolsFragment extends Fragment implements OnClickListener {

	public enum EditorTools {
		MARKER, DRAW, POLY, TRASH, NONE
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
		View view = inflater.inflate(R.layout.fragment_editor_tools, container,
				false);

		buttonDraw = (RadioButton) view.findViewById(R.id.editor_tools_draw);
		buttonMarker = (RadioButton) view
				.findViewById(R.id.editor_tools_marker);
		buttonPoly = (RadioButton) view.findViewById(R.id.editor_tools_poly);
		buttonTrash = (RadioButton) view.findViewById(R.id.editor_tools_trash);

		buttonDraw.setOnClickListener(this);
		buttonMarker.setOnClickListener(this);
		buttonPoly.setOnClickListener(this);
		buttonTrash.setOnClickListener(this);

		buttonMarker.setChecked(true);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listner = (OnEditorToolSelected) activity;
	}

	@Override
	public void onClick(View v) {
		EditorTools newTool = EditorTools.NONE;
		switch (v.getId()) {
		case R.id.editor_tools_marker:
			newTool = EditorTools.MARKER;
			break;
		case R.id.editor_tools_draw:
			newTool = EditorTools.DRAW;
			break;
		case R.id.editor_tools_poly:
			newTool = EditorTools.POLY;
			break;
		case R.id.editor_tools_trash:
			newTool = EditorTools.TRASH;
			break;
		}
		if (newTool == this.tool) {
			newTool = EditorTools.NONE;
		}
		setTool(newTool);
	}

	public EditorTools getTool() {
		return tool;
	}

	public void setTool(EditorTools tool) {
		buttonMarker.setChecked(false);
		buttonDraw.setChecked(false);
		buttonPoly.setChecked(false);
		buttonTrash.setChecked(false);
		switch (tool) {
		case DRAW:
			buttonDraw.setChecked(true);
			break;
		case MARKER:
			buttonMarker.setChecked(true);
			break;
		case POLY:
			buttonPoly.setChecked(true);
			break;
		case TRASH:
			buttonTrash.setChecked(true);
			break;
		case NONE:
			break;
		}
		this.tool = tool;
		listner.editorToolChanged(this.tool);
	}

}
