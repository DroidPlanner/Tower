package com.droidplanner.fragments.calibration;

import com.droidplanner.R;
import com.droidplanner.fragments.SetupFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentCalibration extends Fragment {
	protected SetupFragment parent;
	protected FragmentSetupSidePanel sidePanel;
	protected FragmentManager fragmentManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		fragmentManager = getFragmentManager();
		initSidePanel();
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = getView(inflater, container);
		setupLocalViews(view);
		setupSidePanel();
		return view;
	}

	public SetupFragment getParent() {
		return parent;
	}

	public void setParent(SetupFragment parent) {
		this.parent = parent;
	}

	protected void setupSidePanel() {
		if (sidePanel == null) {
			sidePanel = (FragmentSetupSidePanel) getSidePanel();
			if (sidePanel != null) {
				sidePanel.setParent(this);
				fragmentManager.beginTransaction()
						.add(R.id.fragment_setup_sidepanel, sidePanel).commit();
			}
		} else {
			doUpdateSidePanel();
		}

	}

	private void doUpdateSidePanel() {
		updateSidePanel();
		if(sidePanel!=null){
			fragmentManager.beginTransaction()
			.replace(R.id.fragment_setup_sidepanel, sidePanel).commit();			
		}
	}

	protected abstract View getView(LayoutInflater inflater, ViewGroup container);

	protected abstract void setupLocalViews(View view);

	protected abstract FragmentSetupSidePanel getSidePanel();

	protected abstract void initSidePanel();

	protected abstract void updateSidePanel();

}
