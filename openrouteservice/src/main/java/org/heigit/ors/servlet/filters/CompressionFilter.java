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

public class CompressionFilter implements Filter 
{
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException 
	{
		if (req instanceof HttpServletRequest)
		{
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			String acceptEncoding = request.getHeader("accept-encoding");
			
			if (acceptEncoding != null) {
				/* Commented out as jBrotli library crashes the server.
				 * Java frames: (J=compiled Java code, j=interpreted, Vv=VM code)
                 *J 4868  org.meteogroup.jbrotli.BrotliStreamCompressor.freeNativeResources()I 
				 * if (acceptEncoding.indexOf(ContentEncodingType.BROTLI) != -1) {
					BrotliResponseWrapper wrappedResponse = new BrotliResponseWrapper(response);
					chain.doFilter(req, wrappedResponse);
					wrappedResponse.finishResponse();
					return;
				}
				else*/ if(acceptEncoding.indexOf(ContentEncodingType.GZIP) != -1) {
					GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(response);
					chain.doFilter(req, wrappedResponse);
					wrappedResponse.finishResponse();
					return;
				}
				else if (acceptEncoding.indexOf(ContentEncodingType.DEFLATE) != -1) {
                   // todo
				}
			}

			chain.doFilter(req, res);
		}
	}

	public void init(FilterConfig filterConfig) {

	}

	public void destroy() {

	}
}
