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
package org.heigit.ors.isochrones;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.List;

public class IsochroneMap {
	private final int travellerId;
	private final Envelope envelope;
	private final List<Isochrone> isochrones;
	private final Coordinate center;
	private String graphDate;

    public IsochroneMap(int travellerId, Coordinate center) {
		this.travellerId = travellerId;
		this.center = center;
		isochrones = new ArrayList<>();
		envelope = new Envelope();
	}
	
	public int getTravellerId()
	{
		return travellerId;
	}

	public boolean isEmpty()
	{
		return isochrones.isEmpty();
	}

	public Coordinate getCenter() 
	{
		return center;
	}

    public Iterable<Isochrone> getIsochrones()
	{
		return isochrones;
	}
	
	public int getIsochronesCount()
	{
		return isochrones.size();
	}

	public Isochrone getIsochrone(int index)
	{
		return isochrones.get(index);
	}

	public void addIsochrone(Isochrone isochrone) {
		isochrones.add(isochrone);
		envelope.expandToInclude(isochrone.getGeometry().getEnvelopeInternal());
	}
	public Envelope getEnvelope()
	{
		return envelope;
	}

	public String getGraphDate () {
    	return graphDate;
	}

	public void setGraphDate(String graphDate) {
		this.graphDate = graphDate;
	}
}
