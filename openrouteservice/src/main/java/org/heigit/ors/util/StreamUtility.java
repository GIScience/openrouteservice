/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.util;

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
	private StreamUtility() {}

	public static String readStream(InputStream stream, int bufferSize) throws IOException {
		return readStream(stream, bufferSize, null);
	}

	/**
	 * 
	 * 
	 * @param stream
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
