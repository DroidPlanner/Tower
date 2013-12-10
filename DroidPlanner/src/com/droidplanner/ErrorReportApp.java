package com.droidplanner;

import android.app.Application;

import com.droidplanner.file.IO.ExceptionWriter;

public class ErrorReportApp extends Application {
	private Thread.UncaughtExceptionHandler exceptionHandler;

	private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
		public void uncaughtException(Thread thread, Throwable ex) {
			new ExceptionWriter(ex).saveStackTraceToSD();
			exceptionHandler.uncaughtException(thread, ex);
		}
	};

	public void onCreate() {
		super.onCreate();
		exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

}
