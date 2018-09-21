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
package heigit.ors.geocoding.geocoders;

import com.vividsolutions.jts.geom.Envelope;

public class RectSearchBoundary implements SearchBoundary {
    private Envelope _env;
    
    public RectSearchBoundary(double minx, double miny, double maxx, double maxy)
    {
    	_env = new Envelope(minx, maxx, miny, maxy);
    }
    
    public RectSearchBoundary(Envelope env)
    {
    	_env = env;
    }
    
    public Envelope getRectangle()
    {
    	return _env;
    }
    
    @Override
    public boolean contains(double lon, double lat)
    {
    	return _env.contains(lon,  lat);
    }
}
