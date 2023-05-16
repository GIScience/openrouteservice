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
package org.heigit.ors.matrix;

public class MatrixResult {
  private final float[][] tables;
  private ResolvedLocation[] destinations;
  private ResolvedLocation[] sources;
  private String graphDate;

  public MatrixResult(ResolvedLocation[] sources, ResolvedLocation[] destinations) {
	  tables = new float[6][];
	  this.sources = sources;
	  this.destinations = destinations;
  }

  public void setTable(int metric, float[] values)
  {
	  tables[metric] = values;
  }

  public float[] getTable(int metric)
  {
	  return tables[metric];
  }

  public float[][] getTables() {
      return tables;
  }

  public ResolvedLocation[] getDestinations()
  {
	  return destinations;
  }

  public void setDestinations(ResolvedLocation[] locations)
  {
	  destinations = locations;
  }

  public ResolvedLocation[] getSources()
  {
	  return sources;
  }

  public void setSources(ResolvedLocation[] locations)
  {
	  sources = locations;
  }

  public void setGraphDate(String graphDate) {this.graphDate = graphDate; }

  public String getGraphDate() { return graphDate; }
}
