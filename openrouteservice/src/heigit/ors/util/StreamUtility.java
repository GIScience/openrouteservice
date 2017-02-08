package org.freeopenls.tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import com.graphhopper.util.Helper;

/**
 * <p>
 * <b>Title: StreamUtility</b>
 * </p>
 * <p>
 * <b>Description:</b><br>
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2014 by Maxim Rylov
 * </p>
 * 
 * @author Maxim Rylov, maxim.rylov@geog.uni-heidelberg.de
 * 
 * @version 1.0 2014-05-15
 */
public class StreamUtility {
	public static String readStream(InputStream stream, int bufferSize) throws IOException {
		return readStream(stream, bufferSize, null);
	}

	/**
	 * 
	 * 
	 * @param coordstream
	 *            InputStream
	 * @param bufferSize
	 *            int
	 * @return result String
	 * @throws IOException
	 */
	public static String readStream(InputStream stream, int bufferSize, String encoding) throws IOException {
		StringWriter sw = new StringWriter();
		int bytesRead;

		if (!Helper.isEmpty(encoding)) {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream, encoding), bufferSize);
			String str;
			while ((str = br.readLine()) != null) {
				sw.write(str);
			}
		} else {
			byte[] buffer = new byte[bufferSize];

			while ((bytesRead = stream.read(buffer)) != -1) {
				sw.write(new String(buffer, 0, bytesRead));
			}
		}

		return sw.toString();
	}

	public static byte[] toByteArray(InputStream stream, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int bytesRead;
		while ((bytesRead = stream.read(buffer)) != -1) {
			os.write(buffer, 0, bytesRead);
		}

		os.flush();

		return os.toByteArray();
	}

	public static String readStream(InputStream stream) throws IOException {
		return readStream(stream, 8192);
	}
	
	public static String readStream(InputStream stream, String encoding) throws IOException {
		return readStream(stream, 8192, encoding);
	}
}
