/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

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
