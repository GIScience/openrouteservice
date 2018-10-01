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

