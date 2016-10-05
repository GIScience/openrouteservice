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

// Authors: M. Rylov and ZWang

package org.freeopenls.routeservice.traffic;

public class TrafficEventInfo {
  public int code;
  public String description;
  public byte type;
  private double speedFactor = 1;
  public int category;
  private boolean isDelay = false; 
  private double delay; 
  public int mode = TmcMode.CAR_TMC;
  private boolean isReverse = false;
  
  
  public TrafficEventInfo(int code, String description, int type, double speedFactor, int category)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.speedFactor = speedFactor;	  
	  this.category = category;
  }
  
  public TrafficEventInfo(int code, String description, int type, double speedFactor, boolean isReverse, int category)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.speedFactor = speedFactor;
	  this.isReverse = isReverse; 
	  this.category = category;
  }
  
  
  public TrafficEventInfo(int code, String description, int type, double speedFactor, int mode, int category)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.speedFactor = speedFactor;	  
	  this.mode = mode;
	  this.category = category;
  }
  
  
  public TrafficEventInfo(int code, String description, int type, double speedFactor)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.speedFactor = speedFactor;	  
	  this.category =  TrafficEventCategory.UNDEFINED;
  }
  
  public TrafficEventInfo(int code, String description, int type, boolean isDelay, double delay, int mode, int category)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.isDelay = isDelay; 
	  this.delay = delay;	  
	  this.category =  TrafficEventCategory.UNDEFINED;
	  this.mode = mode; 
  }
  
  public TrafficEventInfo(int code, String description, int type, boolean isDelay, double delay, int category)
  {
	  this.code = code;
	  this.description = description == null ? "" : description.trim();
	  this.type = (byte)type;
	  this.isDelay = isDelay; 
	  this.delay = delay;	  
	  this.category =  TrafficEventCategory.UNDEFINED;

  }
  
  
  public double getSpeedFactor() {
	   return speedFactor;
  }  
  
  public double getDelay() {
	   return delay;
  }
  
  public boolean isDelay() {
	   return isDelay;
  }
  
  public boolean isReverse() {
	   return isReverse;
  }
  
  
  
}
