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

// Authors: M. Rylov

package org.freeopenls.routeservice.traffic;

public class TrafficEventInfo {
  public int code;
  public String description;
  public byte type;
  public float speedFactor;
  public int category;
  
  public TrafficEventInfo(int code, String description, int type, float speedFactor, int category)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.speedFactor = speedFactor;	  
	  this.category = category;
  }
  
  public TrafficEventInfo(int code, String description, int type, float speedFactor)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.speedFactor = speedFactor;	  
	  this.category =  TrafficEventCategory.UNDEFINED;
  }
}
