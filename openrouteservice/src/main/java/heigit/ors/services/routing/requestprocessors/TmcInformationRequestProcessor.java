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
