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

import com.graphhopper.util.Helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public final class HTTPUtility {
	private HTTPUtility() {}

	public static String getResponse(String req, int timeOut, String userAgent, String encoding) throws IOException {
		URL url = new URL(req);
		URLConnection conn = url.openConnection();
		if (!Helper.isEmpty(userAgent))
			conn.setRequestProperty("User-Agent", userAgent);
		if (timeOut > 0)
			conn.setReadTimeout(timeOut);
		// conn.setRequestProperty("Accept-Language", "de,de-de;q=0.8,en;q=0.5,en-us;q=0.3")
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
		String line = "";
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		is.close();
		
		return sb.toString();
	}
}