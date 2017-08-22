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
package heigit.ors.routing;

public class ExtraSummaryItem {
   private double value;
   private double distance;
   private double amount;
   
   public ExtraSummaryItem(double value, double distance, double amount)
   {
	   this.value = value;
	   this.distance = distance;
	   this.amount = amount;
   }
   
   public double getValue()
   {
	   return value;
   }
   
   public double getDistance()
   {
	   return distance;
   }
   
   public double getAmount()
   {
	   return amount;
   }
}
