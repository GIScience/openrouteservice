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
package heigit.ors.services.routing;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.services.routing.requestprocessors.RoutingServiceRequestProcessorFactory;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.http.BaseHttpServlet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated
@RestController
@RequestMapping({"/routes", "/directions"})
public class RoutingServiceServlet extends BaseHttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
	}

	public void destroy() {
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException   {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = RoutingServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}

	@GetMapping
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = RoutingServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}
}
