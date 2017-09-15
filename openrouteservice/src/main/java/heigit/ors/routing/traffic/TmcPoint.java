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

/* Class descriptor: store LCL point information */

package heigit.ors.routing.traffic;

import com.vividsolutions.jts.geom.Coordinate;

public class TmcPoint
{
  private int lcd = -1;
  private int neg_off_lcd = -1;
  private int pos_off_lcd = -1;
  private Coordinate coord = null;
  
  public TmcPoint(int lcd, int neg_off_lcd, int pos_off_lcd) {
    this.lcd = lcd;
    this.neg_off_lcd = neg_off_lcd;
    this.pos_off_lcd = pos_off_lcd;
  }
  
  public String toString() {
    return "LCD: " + this.lcd + " Neg_Off_LCD: " + this.neg_off_lcd + " Pos_Off_LCD: " + this.pos_off_lcd + " Coordinate: " + this.coord;
  }

  public int getLcd()
  {
    return this.lcd;
  }
  
  public int getNeg_off_lcd()
  {
    return this.neg_off_lcd;
  }

  public int getPos_off_lcd()
  {
    return this.pos_off_lcd;
  }
  
  public Coordinate getCoordinate()
  {
    return this.coord;
  }
  
  public void setCoordinate(Coordinate coord)
  {
    this.coord = coord;
  }
}

