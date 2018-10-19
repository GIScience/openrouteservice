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
package heigit.ors.routing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProfileWeightingCollection {
	protected List<ProfileWeighting> _weightings;

	public ProfileWeightingCollection()
	{
		_weightings = new ArrayList<ProfileWeighting>();
	}

	public void add(ProfileWeighting weighting)
	{
		if (_weightings == null)
			_weightings = new ArrayList<ProfileWeighting>();

		_weightings.add(weighting);
	}
	
	public Iterator<ProfileWeighting> getIterator()
	{
		return _weightings.iterator();
	}
	
	public int size()
	{
		return _weightings.size();
	}
}
