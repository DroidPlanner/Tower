package com.droidplanner.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.droidplanner.R;

public class EditorControlFragment extends Fragment implements OnClickListener {

	public interface OnEditorControlInteraction {
		public void editorModeChanged();
	}

	private OnEditorControlInteraction listner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_editor_control,
				container, false);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listner = (OnEditorControlInteraction) activity;
	}

	@Override
	public void onClick(View arg0) {
		listner.editorModeChanged();		
	}



}
