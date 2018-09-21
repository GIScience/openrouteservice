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
public class TrafficEventInfo {
	  public int code;
	  public String description;
	  public byte type;
	  private double speedFactor = 1;  // > 1, given speed or  [0,1] speed factor  
	  public int category;
	  private boolean isDelay = false; 
	  private double delay; 
	  public int mode = TmcMode.CAR;
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
	  
	  public int getTmcMode(){
		   return mode;
	 }
	    	  
	}