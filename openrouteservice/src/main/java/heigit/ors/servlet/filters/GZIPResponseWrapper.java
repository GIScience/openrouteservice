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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class GZIPResponseWrapper extends HttpServletResponseWrapper {
	protected HttpServletResponse _origResponse = null;
	protected GZIPResponseStream _stream = null;
	protected PrintWriter _writer = null;

	public GZIPResponseWrapper(HttpServletResponse response) {
		super(response);
		_origResponse = response;
	}

	public GZIPResponseStream createOutputStream() throws IOException {
		return new GZIPResponseStream(_origResponse);
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
