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
package org.heigit.ors.servlet.filters;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class GZIPResponseWrapper extends HttpServletResponseWrapper {
	protected HttpServletResponse origResponse;
	protected GZIPResponseStream responseStream = null;
	protected PrintWriter writer = null;

	public GZIPResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}

	public GZIPResponseStream createOutputStream() throws IOException {
		return new GZIPResponseStream(origResponse);
	}

	public void finishResponse() {
		try {
			if (writer != null)
				writer.close();
			else {
				if (responseStream != null && !responseStream.isClosed())
					responseStream.close();
			}
		} catch (IOException e) {
			// do nothing
		}
	}

	@Override
	public void flushBuffer() throws IOException {
		if (responseStream != null && !responseStream.isClosed())
			responseStream.flush();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null)
			throw new IllegalStateException("getWriter() has already been called!");

		if (responseStream == null)
			responseStream = createOutputStream();

		return (responseStream);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer != null)
			return (writer);

		if (responseStream != null)
			throw new IllegalStateException("getOutputStream() has already been called!");

		responseStream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(responseStream, StandardCharsets.UTF_8));
		return (writer);
	}

	@Override
	public void setContentLength(int length) {
		// nothing to do
	}
}
