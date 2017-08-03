/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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
	
	public  static final int N = 256;
	public static final int N1 = 512;
	public static final int N2 = 1024;
	public static final int N3 = 2048;
	
	public static final int O = 4096;
	public static final int O1 = 8192;
	public static final int O2 = 16384;
	public static final int O3 = 32768;
	public static final int O4 = 65536;
	
	public static final int General = M;

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
