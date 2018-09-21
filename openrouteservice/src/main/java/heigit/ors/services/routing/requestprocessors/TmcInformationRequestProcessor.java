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
package heigit.ors.services.routing.requestprocessors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.routing.traffic.RealTrafficDataProvider;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;

public class TmcInformationRequestProcessor extends AbstractHttpRequestProcessor 
{
	public TmcInformationRequestProcessor(HttpServletRequest request) throws Exception 
	{
		super(request);
	}

	@Override
	public void process(HttpServletResponse response) throws IOException  
	{
		if (RealTrafficDataProvider.getInstance().isInitialized())
		{
			String bbox =	_request.getParameter("bbox");
			Envelope env = null;
			if (!Helper.isEmpty(bbox))
			{
				String[] bboxValues = bbox.split(",");
				env = new Envelope(Double.parseDouble(bboxValues[0]), Double.parseDouble(bboxValues[2]), Double.parseDouble(bboxValues[1]), Double.parseDouble(bboxValues[3]));
			}

			String json = RealTrafficDataProvider.getInstance().getTmcInfoAsJson(env);

			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().append(json);
		}
		else
		{
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/text");
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().append("Tmc service is unavailable.");

		}
	}
}
