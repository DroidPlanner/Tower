package org.droidplanner.android.activities.interfaces;

/**
 * Implementation of this interface provides user help.
 */
public interface HelpProvider {

	/**
	 * Get help items to be populated
	 * 
	 * @return A matrix with pars of help guides names, with the associated
	 *         video url
	 */
	public abstract CharSequence[][] getHelpItems();
}
