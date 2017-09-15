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