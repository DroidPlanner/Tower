package org.droidplanner.android;

import org.droidplanner.android.utils.file.IO.ExceptionWriter;

import android.app.Application;

public class ErrorReportApp extends Application {
	private Thread.UncaughtExceptionHandler exceptionHandler;

	private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			new ExceptionWriter(ex).saveStackTraceToSD();
			exceptionHandler.uncaughtException(thread, ex);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

}
