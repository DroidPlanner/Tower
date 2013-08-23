package com.srtm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {
	public static void unZipIt(String fname, File output) throws IOException {

		byte[] buffer = new byte[1024];

		ZipFile zip = new ZipFile(output);
		ZipEntry ze = zip.getEntry(fname);
		InputStream zis = zip.getInputStream(ze);

		File newFile = new File(output.getParent() + "/" + fname);

		System.out.println("file unzip : " + newFile.getAbsoluteFile());

		FileOutputStream fos = new FileOutputStream(newFile);

		int len;
		while ((len = zis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}

		fos.close();
		zis.close();
		zip.close();

		System.out.println("Done");
	}
}