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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class BrotliResponseWrapper extends HttpServletResponseWrapper {
	protected HttpServletResponse _origResponse = null;
	protected BrotliResponseStream _stream = null;
	protected PrintWriter _writer = null;

	public BrotliResponseWrapper(HttpServletResponse response) {
		super(response);
		_origResponse = response;
	}

	public BrotliResponseStream createOutputStream() throws IOException {
		return new BrotliResponseStream(_origResponse);
	}

	public void finishResponse() {
		try {
			if (_writer != null) 
				_writer.close();
			else {
				if (_stream != null && !_stream.isClosed()) 
					_stream.close();
			}
		} catch (IOException e) 
		{

		}
	}

	public void flushBuffer() throws IOException {
		if (_stream != null && !_stream.isClosed())
			_stream.flush();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (_writer != null) 
			throw new IllegalStateException("getWriter() has already been called!");

		if (_stream == null)
			_stream = createOutputStream();

		return (_stream);
	}

	public PrintWriter getWriter() throws IOException {
		if (_writer != null) 
			return (_writer);

		if (_stream != null) 
			throw new IllegalStateException("getOutputStream() has already been called!");

		_stream = createOutputStream();
		_writer = new PrintWriter(new OutputStreamWriter(_stream, "UTF-8"));
		return (_writer);
	}

	public void setContentLength(int length) {}
}
