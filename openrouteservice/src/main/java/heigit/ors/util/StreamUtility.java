/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
