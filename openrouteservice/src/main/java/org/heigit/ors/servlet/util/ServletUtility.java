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
package org.heigit.ors.servlet.util;

import org.heigit.ors.common.StatusCode;
import org.heigit.ors.util.StreamUtility;
import org.heigit.ors.util.StringUtility;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServletUtility {

	public static final String KEY_UTF_8 = "UTF-8";

	private ServletUtility() {}

	public static String readRequestContent(HttpServletRequest request) throws IOException {
		InputStream in = request.getInputStream();
		String strDecoded = StringUtility.decodeRequestString(StreamUtility.readStream(in));
		in.close();
		return strDecoded;
	}

	public static void write(HttpServletResponse response, String gpx) throws IOException{
		write(response,gpx, KEY_UTF_8);
	}

	public static void write(HttpServletResponse response, String gpx, String encoding) throws IOException{
		byte[] bytes = gpx.getBytes(encoding);
		write(response, bytes, "application/xml", encoding);
	}

	public static void write(HttpServletResponse response, JSONObject json) throws IOException {
		write(response, json, KEY_UTF_8);
	}

	public static void write(HttpServletResponse response, JSONObject json, String encoding) throws IOException {
		byte[] bytes = json.toString().getBytes(encoding);
		write(response, bytes, "application/json", encoding);
	}

	public static void write(HttpServletResponse response, JSONObject json, int statusCode) throws IOException {
		write(response, json, KEY_UTF_8, statusCode);
	}

	public static void write(HttpServletResponse response, JSONObject json, String encoding, int statusCode) throws IOException {
		byte[] bytes = json.toString().getBytes(encoding);
		write(response, bytes, "application/json", encoding, statusCode);
	}

	public static void write(HttpServletResponse response, byte[] bytes, String contentType) throws IOException {
		write(response, bytes, contentType, KEY_UTF_8);
	}

	public static void write(HttpServletResponse response, byte[] bytes, String contentType, String encoding) throws IOException {
		write (response, bytes, contentType, encoding, StatusCode.OK);
	}

	public static void write(HttpServletResponse response, byte[] bytes, String contentType, String encoding, int statusCode) throws IOException {
		OutputStream outStream = response.getOutputStream();
		response.setHeader("Content-Type", contentType);
		response.setContentLength(bytes.length);
		response.setCharacterEncoding(encoding);
		response.setContentType(contentType);
		response.addHeader("Vary", "Accept-Encoding");

		if (statusCode != StatusCode.OK)
			response.setStatus(statusCode);
		outStream.write(bytes);
		outStream.close();
	}


}