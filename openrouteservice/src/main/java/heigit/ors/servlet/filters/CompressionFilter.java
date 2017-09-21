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
