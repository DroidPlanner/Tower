package org.droidplanner.android.utils.file.IO;

import android.content.Context;

import java.io.PrintStream;

import org.droidplanner.android.utils.file.FileStream;

public class ExceptionWriter {
	private Throwable exception;

	public ExceptionWriter(Throwable ex) {
		this.exception = ex;
	}

	public void saveStackTraceToSD(Context context) {
		try {
			PrintStream out = new PrintStream(FileStream.getExceptionFileStream(context));
			exception.printStackTrace(out);
			out.close();
		} catch (Exception excep) {
			excep.printStackTrace();
		}
	}
}
