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
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import heigit.ors.io.ByteArrayOutputStreamEx;

class GZIPResponseStream extends ServletOutputStream { 
	private ByteArrayOutputStreamEx _bufferStream = null;
	private GZIPOutputStream _gzipstream = null;
	private ServletOutputStream _outputStream = null;
	private HttpServletResponse _response = null;
	private boolean _closed = false;

	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		super();
		
		this._response = response;
		this._outputStream = response.getOutputStream();
		_bufferStream = new ByteArrayOutputStreamEx();
		_gzipstream = new GZIPOutputStream(_bufferStream);
	}

	public void close() throws IOException {
		if (_closed) 
			throw new IOException("This output stream has already been closed");
		
		_gzipstream.finish();

		byte[] bytes = _bufferStream.getBuffer();
		int bytesLength = _bufferStream.size();
				
		_response.setContentLength(bytesLength); 
        _response.addHeader("Content-Encoding", ContentEncodingType.GZIP);

        _outputStream.write(bytes, 0, bytesLength);
        _outputStream.close();
		_closed = true;
	}
	
	public boolean isClosed() {
		return _closed;
	}

	public void flush() throws IOException {
		if (_closed) 
			throw new IOException("Cannot flush a closed output stream");
		
		_gzipstream.flush();
	}

	public void write(int b) throws IOException {
		if (_closed) 
			throw new IOException("Cannot write to a closed output stream");
		
		_gzipstream.write((byte)b);
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (_closed) 
			throw new IOException("Cannot write to a closed output stream");
		
		_gzipstream.write(b, off, len);
	}

	public void reset() {

	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
	}
}