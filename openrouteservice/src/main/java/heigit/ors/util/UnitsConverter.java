/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.util;

public class UnitsConverter {
   public static double SqMetersToSqMiles(double value)  {
	   return value * 3.86102e-7;
   }
   
   public static double SqMetersToSqKilometers(double value)  {
	   return value * 1e-6;
   }
   
   public static double MetersToKilometers(double value) {
	   return value* 0.0001;
   }
   
   public static double MetersToMiles(double value)
   {
	   return value * 0.000621371;
   }
}
