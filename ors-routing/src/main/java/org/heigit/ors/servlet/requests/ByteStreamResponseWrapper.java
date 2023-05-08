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
package org.heigit.ors.servlet.requests;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
 
public class ByteStreamResponseWrapper extends HttpServletResponseWrapper
{
    private ByteArrayOutputStream byteStream;
 
    public ByteStreamResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }
 
    @Override
    public ServletOutputStream getOutputStream()
    {
        ServletOutputStreamImpl outputStream = null;
 
        this.byteStream =  (null == this.byteStream)
            ? new ByteArrayOutputStream() : this.byteStream;
        outputStream = new ServletOutputStreamImpl(this.byteStream);
 
        return (outputStream);
    }
 
    @Override
    public PrintWriter getWriter()
    {
        PrintWriter printWriter = null;
 
        this.byteStream =  (null == this.byteStream)
            ? new ByteArrayOutputStream() : this.byteStream;
        printWriter = new PrintWriter(this.byteStream);
 
        return (printWriter);
    }
 
    @Override
    public String toString()
    {
        return ((null == this.byteStream)
                ? null : this.byteStream.toString());
    }
 
    public byte[] toBytes()
    {
        return ((null == this.byteStream)
                ? null : this.byteStream.toByteArray());
    }
    
	public class ServletOutputStreamImpl extends ServletOutputStream {
		private final OutputStream outputStream;
		private byte[] buffer;

		public ServletOutputStreamImpl(OutputStream out) {
			outputStream = out;
		}


		/**
		 * Writes a byte to the output stream.
		 */
		public final void write(int b) throws IOException {
			outputStream.write(b);
		}

		/**
		 * Writes a byte buffer to the output stream.
		 */
		@Override
		public final void write(byte[] buf, int offset, int len)
				throws IOException {
			outputStream.write(buf, offset, len);
		}

		/**
		 * Prints a string to the stream. Note, this method does not properly
		 * handle character encoding.
		 * 
		 * @param s
		 *            the string to write.
		 */
		@Override
		public void print(String s) throws IOException {
			if (s == null)
				s = "null";

			try (OutputStream out = outputStream) {
				int length = s.length();

				if (buffer == null)
					buffer = new byte[128];

				byte[] localBuffer = this.buffer;

				// server/0810
				int offset = 0;

				while (length > 0) {
					int sublen = localBuffer.length;
					if (length < sublen)
						sublen = length;

					for (int i = 0; i < sublen; i++) {
						localBuffer[i] = (byte) s.charAt(i + offset);
					}

					out.write(localBuffer, 0, sublen);

					length -= sublen;
					offset += sublen;
				}
			}
		}

		@Override
		public final void flush() throws IOException {
			outputStream.flush();
		}

		public String toString() {
			return getClass().getSimpleName() + "[" + outputStream + "]";
		}


		@Override
		public boolean isReady() {
			return true;
		}


		@Override
		public void setWriteListener(WriteListener writeListener) {
			// do nothing
		}
	}
}