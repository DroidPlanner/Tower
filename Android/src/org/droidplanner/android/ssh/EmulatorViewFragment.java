package org.droidplanner.android.ssh;

import org.droidplanner.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class EmulatorViewFragment extends Fragment{
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Get a reference to the parent Activity
		System.out.println("Attach");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		// Initialize the Fragment
		super.onCreate(savedInstanceState);
		System.out.println("onCreate");
	}
	
	View rootView;
	EmulatorView emulatorView;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
							ViewGroup container,
							Bundle savedInstanceState){
		System.out.println("onCreateVIew");

		mHaveFullHwKeyboard = checkHaveFullHwKeyboard(getResources().getConfiguration());

		rootView = inflater.inflate(R.layout.fragment_ssh, container, false);	 
        emulatorView = (EmulatorView) rootView.findViewById(R.id.emulatorview);

		emulatorView.setExtGestureListener(new EmulatorViewGestureListener(emulatorView));
		
		registerForContextMenu(emulatorView);

		return rootView;
	}
	
	// Called once the parent Activity and the FragmentÕs UI have // been created.
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		System.out.println("onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		// Complete the Fragment initialization Ð particularly anything // that requires the parent Activity to be initialized or the // FragmentÕs view to be fully inflated.
	}
	
	@Override
	public void onStart(){
		super.onStart();
		System.out.println("onStart");
		// Apply any required UI change now that the
	}
	// Called at the start of the active lifetime. 
	@Override
	public void onResume(){
		System.out.println("onResume");
		super.onResume();
		// Resume any paused UI updates, threads, or processes required
	}
	// Called at the end of the active lifetime. 
	@Override
	public void onPause(){
		System.out.println("onPause");
		// Suspend UI updates, threads, or CPU intensive processes // that donÕt need to be updated when the Activity isnÕt // the active foreground activity.
		// Persist all edits or state changes
		// as after this call the process is likely to be killed.
		super.onPause(); 
	}
	// Called to save UI state changes at the
	// end of the active lifecycle.
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		System.out.println("onSaveInstanceState");
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate, onCreateView, and
		// onCreateView if the parent Activity is killed and restarted. 
		super.onSaveInstanceState(savedInstanceState);
	}
	
	// Called at the end of the visible lifetime.
	@Override
	public void onStop(){
		System.out.println("onStop");
		// Suspend remaining UI updates, threads, or processing // that arenÕt required when the Fragment isnÕt visible.
		super.onStop();
	}
	// Called when the FragmentÕs View has been detached. 
	@Override
	public void onDestroyView() {
		System.out.println("onDestroyView");
		// Clean up resources related to the View.
		super.onDestroyView();
	}
	
	// Called at the end of the full lifetime.
	@Override
	public void onDestroy(){
		System.out.println("onDestroy");
		// Clean up any resources including ending threads, // closing database connections etc.
		super.onDestroy();
	}
	// Called when the Fragment has been detached from its parent Activity.
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	private boolean mHaveFullHwKeyboard = false;
	private boolean checkHaveFullHwKeyboard(Configuration c) 
	{
		return (c.keyboard == Configuration.KEYBOARD_QWERTY) &&
				(c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
	}
	
	private int mActionBarMode = TermSettings.ACTION_BAR_MODE_NONE; /*TODO actionBarMode*/
	private void doToggleSoftKeyboard() 
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
	}
	
	private class EmulatorViewGestureListener extends android.view.GestureDetector.SimpleOnGestureListener
	{
		private EmulatorView view;

		public EmulatorViewGestureListener(EmulatorView view) 
		{
			this.view = view;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) 
		{
			doUIToggle((int) e.getX(), (int) e.getY(), view.getVisibleWidth(), view.getVisibleHeight());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			return false;
		}
	}
	
	private void doUIToggle(int x, int y, int width, int height) 
	{
		switch (mActionBarMode){
			case TermSettings.ACTION_BAR_MODE_NONE:
				if (android.os.Build.VERSION.SDK_INT >= 11 && (mHaveFullHwKeyboard || y < height / 2)){
					getActivity().openOptionsMenu();
				}else{
					doToggleSoftKeyboard();
				}
				break;
			case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
				if (!mHaveFullHwKeyboard) {
					doToggleSoftKeyboard();
				}
				break;
			case TermSettings.ACTION_BAR_MODE_HIDES:
				if (mHaveFullHwKeyboard || y < height / 2) {
					//TODO doToggleActionBar();
				}else{
					doToggleSoftKeyboard();
				}
				break;
		}
	}
}
