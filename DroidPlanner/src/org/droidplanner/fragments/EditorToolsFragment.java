package org.droidplanner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;

import android.widget.RadioGroup;
import org.droidplanner.R;
import org.droidplanner.widgets.button.RadioButtonCenter;

public class EditorToolsFragment extends Fragment implements OnClickListener {

	public enum EditorTools {
		MARKER, DRAW, POLY, TRASH, NONE
	}

	public interface OnEditorToolSelected {
		public void editorToolChanged(EditorTools tools);
	}

	private OnEditorToolSelected listner;

    private RadioGroup mEditorRadioGroup;
	private RadioButtonCenter buttonDraw;
	private RadioButtonCenter buttonMarker;
	private RadioButtonCenter buttonPoly;
	private RadioButtonCenter buttonTrash;

	private EditorTools tool = EditorTools.MARKER;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_editor_tools, container,
				false);

        mEditorRadioGroup = (RadioGroup) view.findViewById(R.id.editor_tools_layout);
		buttonDraw = (RadioButtonCenter) view.findViewById(R.id.editor_tools_draw);
		buttonMarker = (RadioButtonCenter) view.findViewById(R.id.editor_tools_marker);
		buttonPoly = (RadioButtonCenter) view.findViewById(R.id.editor_tools_poly);
		buttonTrash = (RadioButtonCenter) view.findViewById(R.id.editor_tools_trash);

		buttonDraw.setOnClickListener(this);
		buttonMarker.setOnClickListener(this);
		buttonPoly.setOnClickListener(this);
		buttonTrash.setOnClickListener(this);

        mEditorRadioGroup.check(R.id.editor_tools_marker);
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
            mEditorRadioGroup.clearCheck();
		}
		setTool(newTool);
	}

	public EditorTools getTool() {
		return tool;
	}

	public void setTool(EditorTools tool) {
		this.tool = tool;
		listner.editorToolChanged(this.tool);
	}

}
