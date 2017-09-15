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
package heigit.ors.routing.graphhopper.extensions;

public class TollwayType {
	//https://en.wikipedia.org/wiki/Vehicle_category

	public static final int None = 0;

	public static final int L = 1;
	public static final int L1 = 2;
	public static final int L2 = 4;
	public static final int L3 = 8;
	public static final int L4 = 16;
	public static final int L5 = 32;
	public static final int L6 = 64;
	public static final int L7 = 128;
	
	public  static final int M = 256;
	public static final int M1 = 512;
	public static final int M2 = 1024;
	public static final int M3 = 2048;
	
	public  static final int N = 4096;
	public static final int N1 = 8192;
	public static final int N2 = 16384;
	public static final int N3 = 32768;
	
	public static final int O = 65536;
	public static final int O1 = 131072;
	public static final int O2 = 262144;
	public static final int O3 = 524288;
	public static final int O4 = 1048576;
	
	public static final int General = M;
	
	public static boolean isLType(int flag)
	{
		return TollwayType.isSet(flag, TollwayType.L) || TollwayType.isSet(flag, TollwayType.L1) || TollwayType.isSet(flag, TollwayType.L2) ||
				TollwayType.isSet(flag, TollwayType.L3) || TollwayType.isSet(flag, TollwayType.L4) || TollwayType.isSet(flag, TollwayType.L5) ||
				TollwayType.isSet(flag, TollwayType.L6) || TollwayType.isSet(flag, TollwayType.L7);
	}
	
	public static boolean isMType(int flag)
	{
		return TollwayType.isSet(flag, TollwayType.M) || TollwayType.isSet(flag, TollwayType.M1) || TollwayType.isSet(flag, TollwayType.M2) ||
				TollwayType.isSet(flag, TollwayType.M3);
	}
	
	public static boolean isNType(int flag)
	{
		return TollwayType.isSet(flag, TollwayType.N) || TollwayType.isSet(flag, TollwayType.N1) || TollwayType.isSet(flag, TollwayType.N2) ||
				TollwayType.isSet(flag, TollwayType.N3);
	}
	
	public static boolean isOType(int flag)
	{
		return TollwayType.isSet(flag, TollwayType.O) || TollwayType.isSet(flag, TollwayType.O1) || TollwayType.isSet(flag, TollwayType.O2) ||
				TollwayType.isSet(flag, TollwayType.O3) || TollwayType.isSet(flag, TollwayType.O4);
	}
	
    public static boolean isSet(int flag, int value) {
        return (flag & value) == value;
    }

	public static int getFromString(String value)
	{
		if (value == null)
			return 0;

		switch(value.toLowerCase())
		{
		case "l":
			return L;
		case "l1":
			return L1;
		case "l2":
			return L2;
		case "l3":
			return L3;
		case "l4":
			return L4;
		case "l5":
			return L5;
		case "l6":
			return L6;
		case "l7":
			return L7;
			
		case "M":
			return M;
		case "M1":
			return M1;
		case "M2":
			return M2;
		case "M3":
			return M3;
			
		case "N":
			return N;
		case "N1":
			return N1;
		case "N2":
			return N2;
		case "N3":
			return N3;
			
		case "O":
			return O;
		case "O1":
			return O1;
		case "O2":
			return O2;
		case "O3":
			return O3;
		case "O4":
			return O4;
		}

		return None;
	}
}
