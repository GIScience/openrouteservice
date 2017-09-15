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
