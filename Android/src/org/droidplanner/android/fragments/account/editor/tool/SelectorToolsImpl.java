package org.droidplanner.android.fragments.account.editor.tool;

import android.view.View;
import android.widget.Toast;

import org.droidplanner.android.proxy.mission.item.MissionItemProxy;

import java.util.List;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
class SelectorToolsImpl extends EditorToolsImpl implements View.OnClickListener {

    SelectorToolsImpl(EditorToolsFragment fragment) {
        super(fragment);
    }

    @Override
    public void onListItemClick(MissionItemProxy item) {
        if (missionProxy == null)//미션이 빈거라면
            return;

        if (missionProxy.selection.selectionContains(item))//포함되어 있는것에 있다면
        {
            missionProxy.selection.removeItemFromSelection(item);//지우기
        }
        else
        {
            missionProxy.selection.addToSelection(item);//더하기
        }
    }

    private void selectAll()
    {
        if (missionProxy == null)
            return;

        missionProxy.selection.setSelectionTo(missionProxy.getItems());
        EditorToolsFragment.EditorToolListener listener = editorToolsFragment.listener;
        if (listener != null) //select click start zoom to fit
            listener.zoomToFitSelected();
    }

    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.SELECTOR;
    }

    @Override
    public void setup() {
        EditorToolsFragment.EditorToolListener listener = editorToolsFragment.listener;
        if (listener != null) {
            listener.enableGestureDetection(false);
        }

        Toast.makeText(editorToolsFragment.getContext(), "Click on mission items to select them.",
                Toast.LENGTH_SHORT).show();

        if (missionProxy != null) {//미션이 있으면
            missionProxy.selection.clearSelection();//선택된것을 초기화
            final List<MissionItemProxy> missionItems = missionProxy.getItems();
            editorToolsFragment.selectAll.setEnabled(!missionItems.isEmpty());
        }
    }

    @Override
    public void onClick(View v) {
        //클릭했을때 동작하려는 함수 등등
        selectAll();
    }
}
