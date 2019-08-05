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