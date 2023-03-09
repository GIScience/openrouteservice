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
package org.heigit.ors.util;

import java.util.HashMap;
import java.util.Map;

public class DebugUtility {

	private static final boolean IS_DEBUG;
	private static final Map listMap = new HashMap<String, long[]>();
	static {
		IS_DEBUG = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
				.contains("-agentlib:jdwp");
	}

	private DebugUtility() {}

	public static boolean isDebug()
	{
		return IS_DEBUG;
	}

	public static void setList(String name, final long[] array) {
		listMap.put(name, array);
	}

	public static boolean listContains(String name, final long v) {
		long[] array = (long[])listMap.get(name);
		for (long i : array){
			if(i == v)
				return true;
		}
		return false;
	}
}
