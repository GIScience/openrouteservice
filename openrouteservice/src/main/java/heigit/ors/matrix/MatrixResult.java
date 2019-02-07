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
package heigit.ors.matrix;

public class MatrixResult {
  private float[][] _tables;
  private ResolvedLocation[] _destinations;
  private ResolvedLocation[] _sources;

  public MatrixResult(ResolvedLocation[] sources, ResolvedLocation[] destinations)
  {
	  _tables = new float[6][];
	  _sources = sources;
	  _destinations = destinations;
  }

  public void setTable(int metric, float[] values)
  {
	  _tables[metric] = values;
  }

  public float[] getTable(int metric)
  {
	  return _tables[metric];
  }

  public float[][] getTables() {
      return _tables;
  }

  public ResolvedLocation[] getDestinations()
  {
	  return _destinations;
  }

  public void setDestinations(ResolvedLocation[] locations)
  {
	  _destinations = locations;
  }

  public ResolvedLocation[] getSources()
  {
	  return _sources;
  }

  public void setSources(ResolvedLocation[] locations)
  {
	  _sources = locations;
  }
}
