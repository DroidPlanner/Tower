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

/**
 * This fragment implements and displays the 'tools' used in the editor window to switch between
 * different type of waypoints creation.
 */
public class EditorToolsFragment extends Fragment implements OnClickListener, OnLongClickListener {

    /**
     * Used as key to retrieve the last selected tool from the bundle passed on fragment creation.
     */
    private static final String STATE_SELECTED_TOOL = "selected_tool";

	public enum EditorTools {
		MARKER, DRAW, POLY, TRASH, NONE
	}

	public interface OnEditorToolSelected {
		public void editorToolChanged(EditorTools tools);
		public void editorToolLongClicked(EditorTools tools);
	}

    /**
     * The marker tool should be set by default.
     */
    private static final EditorTools DEFAULT_TOOL = EditorTools.MARKER;

	private OnEditorToolSelected listener;
	private RadioGroup mEditorRadioGroup;
	private EditorTools tool;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_editor_tools, container,	false);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

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

        if(savedInstanceState == null) {
            setToolAndUpdateView(DEFAULT_TOOL);
        }
        else{
            //Retrieve the tool that was last selected before the fragment was destroyed.
            final String toolName = savedInstanceState.getString(STATE_SELECTED_TOOL,
                    DEFAULT_TOOL.name());
            final EditorTools savedTool = EditorTools.valueOf(toolName);
            setToolAndUpdateView(savedTool);
        }
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
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        //Store the currently selected tool
        savedInstanceState.putString(STATE_SELECTED_TOOL, tool.name());
    }

	@Override
	public boolean onLongClick(View v) {
		EditorTools newTool = getToolForView(v.getId());

		if (newTool != EditorTools.NONE) {
			listener.editorToolLongClicked(newTool);
		}

		return false;
	}
	
	@Override
	public void onClick(View v) {
        EditorTools newTool = getToolForView(v.getId());
		if (newTool == this.tool) {
			newTool = EditorTools.NONE;
			mEditorRadioGroup.clearCheck();
		}

		setTool(newTool);
	}

	public EditorTools getTool() {
		return tool;
	}

    /**
     * Updates the selected tool.
     * @param tool selected tool.
     */
	public void setTool(EditorTools tool) {
		setTool(tool, true);
	}

    /**
     * Updates the selected tool, and optionally notify listeners.
     * @param tool selected tool.
     * @param notifyListeners true to notify listeners, false otherwise.
     */
    private void setTool(EditorTools tool, boolean notifyListeners){
        this.tool = tool;
        if (tool == EditorTools.NONE) {
            mEditorRadioGroup.clearCheck();
        }

        if(listener != null && notifyListeners) {
            listener.editorToolChanged(this.tool);
        }
    }

    /**
     * Updates the selected tool, and the view to match.
     * @param tool selected tool.
     */
    private void setToolAndUpdateView(EditorTools tool){
        setTool(tool, false);
        mEditorRadioGroup.check(getViewForTool(tool));
    }

    /**
     * Retrieves the tool matching the selected view.
     * @param viewId id of the selected view.
     * @return matching tool
     */
    private EditorTools getToolForView(int viewId){
        switch (viewId) {
            case R.id.editor_tools_marker:
                return EditorTools.MARKER;

            case R.id.editor_tools_draw:
                return EditorTools.DRAW;

            case R.id.editor_tools_poly:
                return EditorTools.POLY;

            case R.id.editor_tools_trash:
                return EditorTools.TRASH;

            default:
                return EditorTools.NONE;
        }
    }

    /**
     * Retrieves the view matching the selected tool.
     * @param tool selected tool
     * @return matching view id.
     */
    private int getViewForTool(EditorTools tool){
        switch(tool){
            case MARKER:
                return R.id.editor_tools_marker;

            case DRAW:
                return R.id.editor_tools_draw;

            case POLY:
                return R.id.editor_tools_poly;

            case TRASH:
                return R.id.editor_tools_trash;

            case NONE:
            default:
                //Passing -1 to the radio group clear the selected radio button.
                return -1;
        }
    }
}
