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
package heigit.ors.servlet.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.servlet.requests.StatusCodeCaptureWrapper;

public class StatusCodeHandlerFilter implements Filter {

	public StatusCodeHandlerFilter() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		StatusCodeCaptureWrapper responseWrapper = new StatusCodeCaptureWrapper((HttpServletRequest)request, (HttpServletResponse)response);
		Throwable exception = null;

		try {
			chain.doFilter(request, responseWrapper);
		} catch (ServletException e) {
			exception = e.getRootCause();
		} catch (Throwable e) { // NOSONAR this is an UnhandledExceptionHandler - we need to catch this
			exception = e;
		}
		
		if (exception != null)
		{
			// Add further exception processing if needed
		}

		// 	flush to prevent servlet container to add anymore  headers or content
		response.flushBuffer();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}
}
