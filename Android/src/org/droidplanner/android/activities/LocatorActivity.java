package org.droidplanner.android.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenTLogDialog;
import org.droidplanner.android.fragments.LocatorMapFragment;
import org.droidplanner.android.utils.file.IO.TLogReader;

/**
 * This implements the map locator activity. The map locator activity allows the user to find
 * a lost drone using last known GPS positions from the tlogs.
 */
public class LocatorActivity extends SuperUI {

    /*
    View widgets.
     */
	private LocatorMapFragment locatorMapFragment;
	private FragmentManager fragmentManager;
//	private EditorListFragment missionListFragment;
	private TextView infoView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locator);

		fragmentManager = getSupportFragmentManager();

		locatorMapFragment = ((LocatorMapFragment) fragmentManager
				.findFragmentById(R.id.mapFragment));
//		missionListFragment = (EditorListFragment) fragmentManager
//				.findFragmentById(R.id.missionFragment1);
		infoView = (TextView) findViewById(R.id.locatorInfoWindow);
	}

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
//        missionProxy.selection.addSelectionUpdateListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();
//        missionProxy.selection.removeSelectionUpdateListener(this);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_locator, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_zoom_fit_locator:
                locatorMapFragment.zoomToFit();
                return true;

            case R.id.menu_open_tlog_file:
                openLogFile();
                return true;

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    private void openLogFile() {
        OpenFileDialog tlogDialog = new OpenTLogDialog() {
            @Override
            public void tlogFileLoaded(TLogReader reader) {
//                drone.mission.onMissionLoaded(reader.getMsgMissionItems());
                locatorMapFragment.zoomToFit();
            }
        };
        tlogDialog.openDialog(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateMapPadding();
	}

	private void updateMapPadding() {
		int topPadding = infoView.getBottom();
		int rightPadding = 0,bottomPadding = 0;

//		if (missionProxy.getItems().size()>0)
//            bottomPadding = missionListFragment.getView().getHeight();

		locatorMapFragment.setMapPadding(rightPadding, topPadding, 0, bottomPadding);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		locatorMapFragment.saveCameraPosition();
	}

//    @Override
//    public void onSelectionUpdate(List<MissionItemProxy> selected) {
//        final int selectedCount = selected.size();
//
//        missionListFragment.setArrowsVisibility(selectedCount > 0);
//
//        if(selectedCount != 1){
//            removeItemDetail();
//        }
//        else{
//            if(contextualActionBar != null)
//                removeItemDetail();
//            else{
//                showItemDetail(selected.get(0));
//            }
//        }
//
//        planningMapFragment.update();
//    }
}
