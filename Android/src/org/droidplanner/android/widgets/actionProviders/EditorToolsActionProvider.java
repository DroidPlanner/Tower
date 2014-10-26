package org.droidplanner.android.widgets.actionProviders;

import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.droidplanner.R;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.widgets.button.RadioButtonCenter;
import org.droidplanner.core.mission.MissionItemType;

/**
 * Implements and displays the 'tools' used in the editor window
 * to switch between different type of waypoints creation.
 */
public class EditorToolsActionProvider extends ActionProvider implements View.OnClickListener, View.OnLongClickListener {

    public enum EditorTools {
        COMMAND,

        MARKER{
            private final MissionItemType[] supportedTypes = {
                    MissionItemType.WAYPOINT,
                    MissionItemType.SPLINE_WAYPOINT,
                    MissionItemType.LAND,
                    MissionItemType.CIRCLE,
                    MissionItemType.ROI,
                    MissionItemType.CYLINDRICAL_SURVEY
            };

            @Override
            public MissionItemType[] getSupportedMissionItemType(){
                return supportedTypes;
            }
        },

        DRAW{
            private final MissionItemType[] supportedTypes = {
                    MissionItemType.WAYPOINT,
                    MissionItemType.SPLINE_WAYPOINT,
                    MissionItemType.CIRCLE,
                    MissionItemType.SURVEY
            };

            @Override
            public MissionItemType[] getSupportedMissionItemType(){
                return supportedTypes;
            }
        },

        TRASH,

        NONE;

        public int getDefaultMissionItemTypeIndex(){
            return 0;
        }

        public MissionItemType[] getSupportedMissionItemType(){
            return new MissionItemType[0];
        }
    }

    public interface OnEditorToolSelected {
        public void editorToolChanged(EditorTools tools);

        public void editorToolLongClicked(EditorTools tools);
    }

    /**
     * The marker tool should be set by default.
     */
    private static final EditorTools DEFAULT_TOOL = EditorTools.MARKER;

    private final Context context;

    private OnEditorToolSelected listener;
    private RadioGroup mEditorRadioGroup;
    private EditorTools tool = DEFAULT_TOOL;
    private MissionProxy mMissionProxy;

    /**
     * Creates a new instance. ActionProvider classes should always implement a
     * constructor that takes a single Context parameter for inflating from menu XML.
     *
     * @param context Context for accessing resources.
     */
    public EditorToolsActionProvider(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View onCreateActionView() {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.action_provider_editor_tools, null);

        mEditorRadioGroup = (RadioGroup) view.findViewById(R.id.editor_tools_layout);
        final RadioButtonCenter buttonDraw = (RadioButtonCenter) view
                .findViewById(R.id.editor_tools_draw);
        final RadioButtonCenter buttonMarker = (RadioButtonCenter) view
                .findViewById(R.id.editor_tools_marker);
        final RadioButtonCenter buttonTrash = (RadioButtonCenter) view
                .findViewById(R.id.editor_tools_trash);
        final RadioButtonCenter buttonCmd = (RadioButtonCenter) view.findViewById(R.id
                .editor_tools_cmd);

        for (View vv : new View[] { buttonDraw, buttonMarker, buttonTrash, buttonCmd }) {
            vv.setOnClickListener(this);
            vv.setOnLongClickListener(this);
        }

        setToolAndUpdateView(this.tool);

        return view;
    }

    public void initialize(OnEditorToolSelected listener, MissionProxy missionProxy,
                           EditorTools tool){
        mMissionProxy = missionProxy;
        this.listener = listener;
        setToolAndUpdateView(tool);
    }

    @Override
    public void onClick(View v) {
        EditorTools newTool = getToolForView(v.getId());
        if (newTool == this.tool) {
            newTool = EditorTools.NONE;
        }

        setTool(newTool);
    }

    @Override
    public boolean onLongClick(View v) {
        EditorTools newTool = getToolForView(v.getId());

        if (newTool != EditorTools.NONE) {
            listener.editorToolLongClicked(newTool);
        }

        return false;
    }
    public EditorTools getTool() {
        return tool;
    }

    /**
     * Updates the selected tool.
     *
     * @param tool
     *            selected tool.
     */
    public void setTool(EditorTools tool) {
        setTool(tool, true);
    }

    /**
     * Updates the selected tool, and optionally notify listeners.
     *
     * @param tool
     *            selected tool.
     * @param notifyListeners
     *            true to notify listeners, false otherwise.
     */
    private void setTool(EditorTools tool, boolean notifyListeners) {
        if (mMissionProxy != null && mMissionProxy.getItems().size() > 0
                && tool != EditorTools.TRASH && tool != EditorTools.NONE && tool != EditorTools
                .COMMAND) {
            MissionItemProxy lastMissionItem = mMissionProxy.getItems().get(
                    mMissionProxy.getItems().size() - 1);

            switch (lastMissionItem.getMissionItem().getType()) {
                case LAND:
                case RTL:
                    tool = EditorTools.NONE;
                    Toast.makeText(this.context, this.context.getString(R.string
                                    .editor_err_land_rtl_added), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

        this.tool = tool;
        if (tool == EditorTools.NONE && mEditorRadioGroup != null) {
            mEditorRadioGroup.clearCheck();
        }

        if (listener != null && notifyListeners) {
            listener.editorToolChanged(this.tool);
        }
    }

    /**
     * Updates the selected tool, and the view to match.
     *
     * @param tool
     *            selected tool.
     */
    public void setToolAndUpdateView(EditorTools tool) {
        setTool(tool, false);

        if(mEditorRadioGroup != null)
            mEditorRadioGroup.check(getViewForTool(tool));
    }

    /**
     * Retrieves the tool matching the selected view.
     *
     * @param viewId
     *            id of the selected view.
     * @return matching tool
     */
    private EditorTools getToolForView(int viewId) {
        switch (viewId) {
            case R.id.editor_tools_marker:
                return EditorTools.MARKER;

            case R.id.editor_tools_draw:
                return EditorTools.DRAW;

            case R.id.editor_tools_trash:
                return EditorTools.TRASH;

            case R.id.editor_tools_cmd:
                return EditorTools.COMMAND;

            default:
                return EditorTools.NONE;
        }
    }

    /**
     * Retrieves the view matching the selected tool.
     *
     * @param tool
     *            selected tool
     * @return matching view id.
     */
    private int getViewForTool(EditorTools tool) {
        switch (tool) {
            case MARKER:
                return R.id.editor_tools_marker;

            case DRAW:
                return R.id.editor_tools_draw;

            case TRASH:
                return R.id.editor_tools_trash;

            case COMMAND:
                return R.id.editor_tools_cmd;

            case NONE:
            default:
                // Passing -1 to the radio group clear the selected radio button.
                return -1;
        }
    }
}

