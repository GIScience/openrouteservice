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
package heigit.ors.routing.traffic;

public class TrafficEventCategory {
	public static final int UNDEFINED = 0;
	public static final int NORMAL_TRAFFIC = 1;
	public static final int WARNING = 2;
	public static final int ROADWORKS = 3;
	public static final int PARTIALLY_CLOSED  = 4;
	public static final int SLOW_TRAFFIC = 5;
	public static final int STATIONARY_TRAFFIC = 6;
	public static final int COMPLETELY_CLOSED  = 7;
	
	public static String toString(int category)
	{
		switch(category)
		{
			case UNDEFINED:
				return "UNDEFINED";
			case WARNING:
				return "WARNING";
			case STATIONARY_TRAFFIC:
				return "STATIONARY_TRAFFIC";
			case SLOW_TRAFFIC:
				return "SLOW_TRAFFIC";
			case NORMAL_TRAFFIC:
				return "NORMAL_TRAFFIC";
			case ROADWORKS:
				return "ROADWORKS";
			case PARTIALLY_CLOSED:
				return "PARTIALLY_CLOSED";
			case COMPLETELY_CLOSED:
				return "COMPLETELY_CLOSED";
		}
		
		return null;
	}
}
