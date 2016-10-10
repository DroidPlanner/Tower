package org.droidplanner.android.fragments.account.editor.tool;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
class NoneToolsImpl extends EditorToolsImpl {

    NoneToolsImpl(EditorToolsFragment fragment) {
        super(fragment);
    }

    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.NONE;
    }

    @Override
    public void setup() {
        EditorToolsFragment.EditorToolListener listener = editorToolsFragment.listener;
        if (listener != null) {
            listener.enableGestureDetection(false);
        }
    }
}
