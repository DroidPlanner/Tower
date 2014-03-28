package org.droidplanner.android.utils.file.IO;

import java.io.PrintStream;

import org.droidplanner.android.utils.file.FileStream;

public class ExceptionWriter {
	private Throwable exception;

	public ExceptionWriter(Throwable ex) {
		this.exception = ex;
	}

	public void saveStackTraceToSD() {
		try {
			PrintStream out = new PrintStream(
					FileStream.getExceptionFileStream());
			exception.printStackTrace(out);
			out.close();
		} catch (Exception excep) {
			excep.printStackTrace();
		}
	}
}
