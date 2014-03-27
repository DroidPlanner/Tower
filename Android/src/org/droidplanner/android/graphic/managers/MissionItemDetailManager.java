package org.droidplanner.android.graphic.managers;

import org.droidplanner.R.id;
import org.droidplanner.android.activities.EditorActivity;
import org.droidplanner.android.fragments.mission.MissionDetailFragment;
import org.droidplanner.android.graphic.EditorMissionItem;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.mission.MissionItem;

import android.support.v4.app.FragmentManager;
import android.view.View;

public class MissionItemDetailManager {

	private MissionDetailFragment itemDetailFragment;
	private View view;
	private FragmentManager fragmentManager;

	/**
	 * @param detailView
	 *            On phone, this view will be null causing the item detail to be
	 *            shown as a dialog.
	 */
	public MissionItemDetailManager(View detailView,
			FragmentManager fragmentManager) {
		view = detailView;
		this.fragmentManager = fragmentManager;
	}

	public void showItemDetail(MissionItem item) {
		if (itemDetailFragment == null) {
			addItemDetail(item);
		} else {
			switchItemDetail(item);
		}
	}

	public void switchItemDetail(MissionItem item) {
		removeItemDetail();
		addItemDetail(item);
	}

	public void removeItemDetail() {
		if (itemDetailFragment != null) {
			if (view == null) {
				itemDetailFragment.dismiss();
			} else {
				fragmentManager.beginTransaction().remove(itemDetailFragment)
						.commit();
			}
			itemDetailFragment = null;
		}
	}

	public void removeDetailIfItemIsRemoved(Drone drone) {
		if (itemDetailFragment != null) {
			if (!drone.mission.hasItem(itemDetailFragment.getItem())) {
				removeItemDetail();
			}
		}
	}

	public MissionDetailFragment getItemDetailFragment(
			EditorActivity editorActivity) {
		return itemDetailFragment;
	}

	private void addItemDetail(MissionItem item) {
		if (item instanceof EditorMissionItem) {
			itemDetailFragment = ((EditorMissionItem) item).getDetailFragment();
	
			if (view == null) {
				itemDetailFragment.show(fragmentManager, "Item detail dialog");
			} else {
				fragmentManager.beginTransaction()
						.add(id.containerItemDetail, itemDetailFragment)
						.commit();
			}
		}
	}

}
