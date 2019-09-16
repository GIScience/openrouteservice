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
package org.heigit.ors.servlet.http;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONObject;

import org.heigit.ors.common.StatusCode;
import org.heigit.ors.routing.RoutingProfileManagerStatus;
import org.heigit.ors.servlet.util.ServletUtility;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @deprecated
 */
@Deprecated
@RestController
@RequestMapping("/health")
public class HealthStatusServlet extends BaseHttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		// do nothing
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		// do nothing
	}

	@GetMapping
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			JSONObject jStatus = new JSONObject();

			if (!RoutingProfileManagerStatus.isReady())
			{
				jStatus.put("status", "not ready");
				ServletUtility.write(response, jStatus, StatusCode.SERVICE_UNAVAILABLE);
			}
			else
			{
				jStatus.put("status", "ready");
				ServletUtility.write(response, jStatus, StatusCode.OK);
			}
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}
}
