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
