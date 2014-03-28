package org.droidplanner.fragments;

import org.droidplanner.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class InputDialogFragment extends DialogFragment {
    
    public interface Listener {
        void onCancel();
        void onInput(String text);
    }
    
    public static InputDialogFragment newInstance(
            String title, String message, Listener listener) {
        InputDialogFragment frag = new InputDialogFragment();
        frag.mListener = listener;
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }
    
    private Listener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View v = li.inflate(R.layout.fragment_input_dialog, null);
        final EditText text = (EditText)v.findViewById(R.id.edit_input);
        
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(getArguments().getString("title"))
            .setMessage(getArguments().getString("message"))
            .setView(v)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mListener != null) {
                        String str = text.getText().toString();
                        mListener.onInput(str);
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
            });
            
        return b.create();
    }
}
