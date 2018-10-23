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
package heigit.ors.io;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStreamEx extends ByteArrayOutputStream  
{
	public ByteArrayOutputStreamEx() 
	{
		super(); 
	}

	public ByteArrayOutputStreamEx(int size) 
	{ 
		super(size); 
	}

	/** Returns the internal buffer of this ByteArrayOutputStream, without copying. */
	public synchronized byte[] getBuffer() 
	{
		return this.buf;
	}
}
