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
package heigit.ors.services.isochrones;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import heigit.ors.services.isochrones.requestprocessors.IsochronesServiceRequestProcessorFactory;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.http.BaseHttpServlet;

public class IsochronesServiceServlet extends BaseHttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 12342412347892L;

	public void init() {
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException 
	{
		try
		{
			AbstractHttpRequestProcessor reqProcessor = IsochronesServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch(Exception ex)
		{
			writeError(response, ex);
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		try
		{
			AbstractHttpRequestProcessor reqProcessor = IsochronesServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch(Exception ex)
		{
			writeError(response, ex);
		}
	}

    public void destroy() {
    	
    }
}
