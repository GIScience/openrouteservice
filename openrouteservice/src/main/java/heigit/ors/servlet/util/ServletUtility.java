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
package heigit.ors.servlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import heigit.ors.util.StreamUtility;
import heigit.ors.util.StringUtility;
 
public class ServletUtility
{
	public static String readRequestContent(HttpServletRequest request) throws IOException
	{
		InputStream in = request.getInputStream();
		String strDecoded = StringUtility.decodeRequestString(StreamUtility.readStream(in));
		in.close();
		
		return strDecoded;
	}
	
	public static void write(HttpServletResponse response, JSONObject json) throws IOException
	{
	  write(response, json, "UTF-8");
	}
	
	public static void write(HttpServletResponse response, JSONObject json, String encoding) throws IOException
	{
		byte[] bytes = json.toString().getBytes(encoding);
		write(response, bytes, "text/json", encoding);

		/*
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
		Writer writer = new OutputStreamWriter(byteStream, "UTF-8");
		json.write(writer);
		writer.flush();

		response.setHeader("Content-Type", "text/json");
		response.setContentLength(byteStream.size());
		response.setCharacterEncoding(encoding);
		response.setContentType("text/json");
		
		OutputStream outStream = response.getOutputStream();
		byteStream.writeTo(outStream);
		outStream.close();*/
	}
	
	public static void write(HttpServletResponse response, byte[] bytes, String contentType) throws IOException
	{
		write(response, bytes, contentType, "UTF-8");
	}
	
	public static void write(HttpServletResponse response, byte[] bytes, String contentType, String encoding) throws IOException
	{
		OutputStream outStream = response.getOutputStream();
		response.setHeader("Content-Type", contentType);
		response.setContentLength(bytes.length);
		response.setCharacterEncoding(encoding);
		response.setContentType(contentType);
		outStream.write(bytes);
		outStream.close();
	}
}