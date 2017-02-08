package org.freeopenls.tools;

import java.io.UnsupportedEncodingException;

public class StringUtility {

	public static boolean isEmpty( String str )
    {
        return str == null || str.trim().length() == 0;
    }
	
	public static String decodeRequestString(String inputString) throws UnsupportedEncodingException {
		if (inputString.startsWith("REQUEST=")) {
			inputString = inputString.substring(8, inputString.length());
		}
		if (inputString.startsWith("xml=")) {
			inputString = inputString.substring(4, inputString.length());
		} else if (inputString.startsWith("---")) {
			int iIndexStart = inputString.indexOf("<?xml");
			inputString = inputString.substring(iIndexStart, inputString.length());
			int iIndexEnd = inputString.indexOf("---");
			inputString = inputString.substring(0, iIndexEnd);
		}

		// Decode the application/x-www-form-url encoded query string
		return java.net.URLDecoder.decode(inputString, "ISO-8859-1");// "UTF-8");
	}

	public static String decodeRequestString2(String inputString) throws UnsupportedEncodingException {
		if (inputString.startsWith("REQUEST=")) {
			inputString = inputString.substring(8, inputString.length());
		} else if (inputString.startsWith("---")) {
			int iIndexStart = inputString.indexOf("<?xml");
			inputString = inputString.substring(iIndexStart, inputString.length());
			int iIndexEnd = inputString.indexOf("---");
			inputString = inputString.substring(0, iIndexEnd);
		}

		// Decode the application/x-www-form-url encoded query string
		return java.net.URLDecoder.decode(inputString, "ISO-8859-1");// "UTF-8");
	}
}
