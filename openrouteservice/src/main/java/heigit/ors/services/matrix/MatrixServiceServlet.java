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
package heigit.ors.services.matrix;

import javax.servlet.*;
import javax.servlet.http.*;

import heigit.ors.services.matrix.requestprocessors.MatrixServiceRequestProcessorFactory;
import heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import heigit.ors.servlet.http.BaseHttpServlet;

public class MatrixServiceServlet extends BaseHttpServlet {
	/** Serial Version UID */
	private static final long serialVersionUID = 1243348952345L;

	public void init() throws ServletException {
	}

	public void destroy() {
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException   {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = MatrixServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try
		{
			AbstractHttpRequestProcessor reqProcessor = MatrixServiceRequestProcessorFactory.createProcessor(request);
			reqProcessor.process(response);
			reqProcessor.destroy();
		}
		catch (Exception ex) {
			writeError(response, ex);
		}
	}
}
