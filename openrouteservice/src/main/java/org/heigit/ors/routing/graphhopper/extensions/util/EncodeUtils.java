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
package org.heigit.ors.routing.graphhopper.extensions.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class EncodeUtils {
	private EncodeUtils() {}

	/**
	 * Takes a long value and converts it into a byte array of size 8.
	 *
	 * @param longValue
	 * @return			An 8 long byte array representation of the long number
	 */
	public static byte[] longToByteArray(long longValue) {
		ByteBuffer longToByteBuffer = ByteBuffer.allocate(Long.BYTES);
		longToByteBuffer.putLong(longValue);
		return longToByteBuffer.array();
	}

	/**
	 * Takes a byte array and converts it to a long value representation
	 *
	 * @param byteArray
	 * @return			The long number representation of the bytes
	 */
	public static long byteArrayToLong(byte[] byteArray) {
		ByteBuffer byteToLongBuffer = ByteBuffer.allocate(Long.BYTES);
		// Need to make up to the needed 8 bytes
		byte[] storageBytes = {0,0,0,0,0,0,0,0};
		int differenceInSize = storageBytes.length - byteArray.length;

		for(int i = byteArray.length-1; i >= 0; i--) {
			if(differenceInSize + i >= 0)
				storageBytes[differenceInSize + i] = byteArray[i];
		}

		byteToLongBuffer.put(storageBytes);
		((Buffer)byteToLongBuffer).flip(); // Changes in Java 9 make the cast to Buffer necessary
		return byteToLongBuffer.getLong();
	}
}
