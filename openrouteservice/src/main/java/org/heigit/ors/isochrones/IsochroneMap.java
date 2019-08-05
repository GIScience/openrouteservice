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
package heigit.ors.isochrones;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.List;

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
