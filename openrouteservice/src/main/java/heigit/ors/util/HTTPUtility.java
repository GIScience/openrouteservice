/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;

public final class HTTPUtility {

	public static String getResponse(String req, String userAgent) throws IOException
	{
	   return getResponse(req, 2000, userAgent, "UTF-8");
	}
	
	public static String getResponse(String req, int timeOut, String userAgent) throws IOException
	{
		return getResponse(req, timeOut, userAgent, "UTF-8");
	}
	
	public static String getResponse(String req, int timeOut, String userAgent, String encoding) throws IOException
	{
		URL url = new URL(req);
		URLConnection conn = url.openConnection();
		if (!Helper.isEmpty(userAgent))
			conn.setRequestProperty("User-Agent", userAgent);
		if (timeOut > 0)
			conn.setReadTimeout(timeOut);
		// conn.setRequestProperty("Accept-Language",
		// "de,de-de;q=0.8,en;q=0.5,en-us;q=0.3");
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
		String line = "";
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		is.close();
		
		return sb.toString();
	}
	
	public static String getRemoteAddr(HttpServletRequest req) {
		if (checkIP(req, "HTTP_CLIENT_IP")) {
			return getIpAddressFromList(req.getHeader("HTTP_CLIENT_IP"));
		}
		
		if (checkIP(req, "X-Forwarded-For")) {
			return getIpAddressFromList(req.getHeader("X-Forwarded-For"));
		}
		
		if (checkIP(req, "Proxy-Client-IP")) {
			return getIpAddressFromList(req.getHeader("Proxy-Client-IP"));
		}
		
		if (checkIP(req, "HTTP_X_FORWARDED_FOR")) {
			return getIpAddressFromList(req.getHeader("HTTP_X_FORWARDED_FOR"));
		}
		if (checkIP(req, "HTTP_X_FORWARDED")) {
			return getIpAddressFromList(req.getHeader("HTTP_X_FORWARDED"));
		} else if (checkIP(req, "HTTP_X_CLUSTER_CLIENT_IP")) {
			return req.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
		} else if (checkIP(req, "HTTP_FORWARDED_FOR")) {
			return getIpAddressFromList(req.getHeader("HTTP_FORWARDED_FOR"));
		} else if (checkIP(req, "HTTP_FORWARDED")) {
			return getIpAddressFromList(req.getHeader("HTTP_FORWARDED"));
		} else {
			return req.getRemoteAddr();
		}
	}
	
	private static String getIpAddressFromList(String ipaddresses) {
		if (Helper.isEmpty(ipaddresses))
			return ipaddresses;

		 String remoteAddr = ipaddresses;
	      int idx = 0;
	      int nLength = ipaddresses.length();

	      while (idx < nLength)
	      {
	        int prevPos = idx;
	        int pos = ipaddresses.indexOf(',', idx);

	        if (pos > -1)
	        {
	          remoteAddr = ipaddresses.substring(prevPos, pos);
	          if (remoteAddr != null)
	            remoteAddr = remoteAddr.trim();

	          if (remoteAddr != "127.0.0.1" && remoteAddr != "129.206.228.69")
	          {
	            break;
	          }

	          idx = pos + 1;
	        }
	        else
	        {
	          break;
	        }
	      }

		return remoteAddr;
	}

	private static boolean checkIP(HttpServletRequest req, String headerAttrNm) {
		String ip = req.getHeader(headerAttrNm);
		if (ip != null && !ip.isEmpty()) {
			return true;
		}
		return false;
	}
}