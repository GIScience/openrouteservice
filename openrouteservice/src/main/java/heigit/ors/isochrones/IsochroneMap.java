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
package heigit.ors.isochrones;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class IsochroneMap {
	private int _travellerId;
	private Envelope _envelope;
	private List<Isochrone> _isochrones;
	private Coordinate _center;
	
	public IsochroneMap(int travellerId, Coordinate center)
	{
		_travellerId = travellerId;
		_center = center;
		_isochrones = new ArrayList<Isochrone>();
		_envelope = new Envelope();
	}
	
	public int getTravellerId()
	{
		return _travellerId;
	}

	public boolean isEmpty()
	{
		return _isochrones.size() == 0;
	}

	public Coordinate getCenter() 
	{
		return _center;
	}

	public Iterable<Isochrone> getIsochrones()
	{
		return _isochrones;
	}
	
	public int getIsochronesCount()
	{
		return _isochrones.size();
	}

	public Isochrone getIsochrone(int index)
	{
		return _isochrones.get(index);
	}

	public void addIsochrone(Isochrone isochrone)
	{
		_isochrones.add(isochrone);
		_envelope.expandToInclude(isochrone.getGeometry().getEnvelopeInternal());
	}

	public Envelope getEnvelope()
	{
		return _envelope;
	}
}
