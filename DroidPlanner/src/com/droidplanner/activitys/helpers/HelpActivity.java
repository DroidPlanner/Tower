package com.droidplanner.activitys.helpers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

import com.droidplanner.R;
import android.support.v4.app.FragmentActivity;

public abstract class HelpActivity extends FragmentActivity implements OnClickListener {

	public HelpActivity() {
		super();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
				if (item.getItemId() == R.id.menu_help) {
					showHelpDialog();
				}
		return super.onOptionsItemSelected(item);
	}

	private void showHelpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.help);
		builder.setItems(getHelpItems()[0], this);
		builder.create().show();		
	}

	/**
	 * Get help items to be populated
	 * 
	 * @return A matrix with pars of help guides names, with the associated
	 *         video url
	 */
	public abstract CharSequence[][] getHelpItems();

	@Override
	public void onClick(DialogInterface dialog, int which) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getHelpItems()[1][which].toString())));		
	}

}