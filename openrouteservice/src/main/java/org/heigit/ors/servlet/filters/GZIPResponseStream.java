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
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import org.heigit.ors.io.ByteArrayOutputStreamEx;

class GZIPResponseStream extends ServletOutputStream { 
	private ByteArrayOutputStreamEx bufferStream = null;
	private GZIPOutputStream gzipOutputStream = null;
	private ServletOutputStream servletOutputStream = null;
	private HttpServletResponse servletResponse = null;
	private boolean closed = false;

	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		super();
		
		this.servletResponse = response;
		this.servletOutputStream = response.getOutputStream();
		bufferStream = new ByteArrayOutputStreamEx();
		gzipOutputStream = new GZIPOutputStream(bufferStream);
	}

	@Override
	public void close() throws IOException {
		if (closed)
			throw new IOException("This output stream has already been closed");
		
		gzipOutputStream.finish();

		byte[] bytes = bufferStream.getBuffer();
		int bytesLength = bufferStream.size();
				
		servletResponse.setContentLength(bytesLength);
        servletResponse.addHeader("Content-Encoding", ContentEncodingType.GZIP);

        servletOutputStream.write(bytes, 0, bytesLength);
        servletOutputStream.close();
		closed = true;
	}
	
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void flush() throws IOException {
		if (closed)
			return; // already closed, nothing to do
		
		gzipOutputStream.flush();
	}

	public void write(int b) throws IOException {
		if (closed)
			throw new IOException("Cannot write to a closed output stream");
		
		gzipOutputStream.write((byte)b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (closed)
			throw new IOException("Cannot write to a closed output stream");
		
		gzipOutputStream.write(b, off, len);
	}

	public void reset() {
		// nothing to do
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
		// nothing to do
	}
}