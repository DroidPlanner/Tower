package org.droidplanner.core.model;

/**
 * Defines a set of essential logging utilities.
 */
public interface Logger {

	public void logVerbose(String logTag, String verbose);

	public void logDebug(String logTag, String debug);

	public void logInfo(String logTag, String info);

	public void logWarning(String logTag, String warning);

	public void logWarning(String logTag, Exception exception);

	public void logWarning(String logTag, String warning, Exception exception);

	public void logErr(String logTag, String err);

	public void logErr(String logTag, Exception exception);

	public void logErr(String logTag, String err, Exception exception);
}
