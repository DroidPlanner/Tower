package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.button.RadioButtonCenter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class EditorToolsFragment extends Fragment implements OnClickListener, OnLongClickListener {

	public enum EditorTools {
		MARKER, DRAW, POLY, TRASH, NONE
	}

	public interface OnEditorToolSelected {
		public void editorToolChanged(EditorTools tools);
		public void editorToolLongClicked(EditorTools tools);
	}

	private OnEditorToolSelected listener;

	private RadioGroup mEditorRadioGroup;

	private EditorTools tool;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_editor_tools, container, false);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mEditorRadioGroup = (RadioGroup) view.findViewById(R.id.editor_tools_layout);
        final RadioButtonCenter buttonDraw = (RadioButtonCenter) view
                .findViewById(R.id.editor_tools_draw);
        final RadioButtonCenter buttonMarker = (RadioButtonCenter) view
                .findViewById(R.id.editor_tools_marker);
        final RadioButtonCenter buttonPoly = (RadioButtonCenter) view
                .findViewById(R.id.editor_tools_poly);
        final RadioButtonCenter buttonTrash = (RadioButtonCenter) view
                .findViewById(R.id.editor_tools_trash);

        for (View vv : new View[] { buttonDraw, buttonMarker, buttonPoly,
                buttonTrash }) {
            vv.setOnClickListener(this);
            vv.setOnLongClickListener(this);
        }

        mEditorRadioGroup.check(R.id.editor_tools_marker);
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        if(!(activity instanceof OnEditorToolSelected)){
            throw new IllegalStateException("Parent activity must be an instance of " +
                    OnEditorToolSelected.class.getName());
        }

		listener = (OnEditorToolSelected) activity;
	}

    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
    }

	@Override
	public boolean onLongClick(View v) {
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

		if (newTool != EditorTools.NONE) {
			listener.editorToolLongClicked(newTool);
		}

		return false;
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
		if (tool == EditorTools.NONE) {
			mEditorRadioGroup.clearCheck();
		}
		listener.editorToolChanged(this.tool);
	}
}
