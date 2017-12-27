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
package heigit.ors.servlet.util;

import heigit.ors.common.StatusCode;
import heigit.ors.util.StreamUtility;
import heigit.ors.util.StringUtility;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ServletUtility
{
	public static String readRequestContent(HttpServletRequest request) throws IOException
	{
		InputStream in = request.getInputStream();
		String strDecoded = StringUtility.decodeRequestString(StreamUtility.readStream(in));
		in.close();

		return strDecoded;
	}

	public static void write(HttpServletResponse response, String gpx) throws IOException{
		write(response,gpx, "UTF-8");
	}
	public static void write(HttpServletResponse response, String gpx, String encoding) throws IOException{
		byte[] bytes = gpx.getBytes(encoding);
		write(response, bytes, "application/xml", encoding);
	}

	public static void write(HttpServletResponse response, JSONObject json) throws IOException
	{
		write(response, json, "UTF-8");
	}

	public static void write(HttpServletResponse response, JSONObject json, String encoding) throws IOException
	{
		byte[] bytes = json.toString().getBytes(encoding);
		write(response, bytes, "application/json", encoding);
	}

	public static void write(HttpServletResponse response, JSONObject json, int statusCode) throws IOException
	{
		write(response, json, "UTF-8", statusCode);
	}

	public static void write(HttpServletResponse response, JSONObject json, String encoding, int statusCode) throws IOException
	{
		byte[] bytes = json.toString().getBytes(encoding);
		write(response, bytes, "application/json", encoding, statusCode);
	}

	public static void write(HttpServletResponse response, byte[] bytes, String contentType) throws IOException
	{
		write(response, bytes, contentType, "UTF-8");
	}

	public static void write(HttpServletResponse response, byte[] bytes, String contentType, String encoding) throws IOException
	{
		write (response, bytes, contentType, encoding, StatusCode.OK);
	}

	public static void write(HttpServletResponse response, byte[] bytes, String contentType, String encoding, int statusCode) throws IOException
	{

		//TODO Add content type
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