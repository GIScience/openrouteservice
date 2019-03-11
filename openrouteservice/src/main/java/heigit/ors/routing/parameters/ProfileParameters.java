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
package heigit.ors.routing.parameters;

import heigit.ors.routing.ProfileWeighting;
import heigit.ors.routing.ProfileWeightingCollection;

import java.util.ArrayList;
import java.util.List;

public class ProfileParameters {
    protected ProfileWeightingCollection _weightings;

    public void add(ProfileWeighting weighting)
    {
    	if (_weightings == null)
    		_weightings = new ProfileWeightingCollection();
    	
    	_weightings.add(weighting);
    }
    
    public ProfileWeightingCollection getWeightings()
    {
    	return _weightings;
    }
    
    public boolean hasWeightings()
    {
    	return _weightings != null && _weightings.size() > 0;
    }

    public List<String> getValidRestrictions() {
        List<String> valid = new ArrayList<>();
        return valid;
    }
}
