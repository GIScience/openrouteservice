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
