package org.droidplanner.fragments;

import java.util.ArrayList;

import org.droidplanner.R;
import org.droidplanner.helpers.MissionFiles;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MissionFileSelectFragment extends DialogFragment {
    
    public interface Listener {
        void onCancel();
        void onFileSelected(CharSequence name);
        void onDeleteFiles(CharSequence[] names);
    }
    
    public static MissionFileSelectFragment newInstance(
            String title, String message, Listener listener) {
        MissionFileSelectFragment f = new MissionFileSelectFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        f.setArguments(args);
        f.mListener = listener;
        return f;
    }
    
    private Listener mListener;
    
    private CharSequence[] getSelectedNames(CharSequence[] src, boolean[] selections) {
        final ArrayList<CharSequence> sel = new ArrayList<CharSequence>();
        
        final int size = src.length;
        for(int i = 0; i < size; ++i) {
            if(selections[i]) {
                sel.add(src[i]);
            }
        }
        
        return sel.toArray(new CharSequence[sel.size()]);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        final CharSequence[] names = MissionFiles.getNames();
        final boolean[] checkedItems = new boolean[names.length];
        final Dialog[] dlg = new Dialog[1];
        
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
            .setTitle(getArguments().getString("title"))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mListener != null) {
                        mListener.onFileSelected(getSelectedNames(names, checkedItems)[0]);
                    }
                }
            })
            .setNeutralButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mListener != null) {
                        mListener.onDeleteFiles(getSelectedNames(names, checkedItems));
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mListener != null) {
                        mListener.onCancel();
                    }
                }
            })
            .setMultiChoiceItems(names, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    // Only allow someone to load a single file, but delete multiples.
                    ((AlertDialog)dlg[0]).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(getSelectedNames(names, checkedItems).length == 1);
                }
            })
            ;
        
        dlg[0] = b.create();
        
        return dlg[0];
    }
}
