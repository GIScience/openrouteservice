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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.graphhopper.extensions.AccessRestrictionType;
import heigit.ors.routing.graphhopper.extensions.storages.AccessRestrictionsGraphStorage;

public class AccessRestrictionsGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private AccessRestrictionsGraphStorage _storage;
	private boolean _hasRestrictions = false;
	private int[] _restrictions = new int[4];
	private List<String> _accessRestrictedTags = new ArrayList<String>(5);
	private List<String> _motorCarTags = new ArrayList<String>(5);
	private List<String> _motorCycleTags = new ArrayList<String>(5);
	private Set<String> _restrictedValues = new HashSet<String>(5);
	private Set<String> _permissiveValues = new HashSet<String>(5);

	public AccessRestrictionsGraphStorageBuilder()
	{
		_accessRestrictedTags.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));
		_motorCarTags.addAll(Arrays.asList("motorcar", "motor_vehicle"));
		_motorCycleTags.addAll(Arrays.asList("motorcycle", "motor_vehicle"));

		_restrictedValues.add("private");
		_restrictedValues.add("no");
		_restrictedValues.add("restricted");
		_restrictedValues.add("military");
		_restrictedValues.add("destination");
		_restrictedValues.add("customers");
		_restrictedValues.add("emergency");

		_permissiveValues.add("yes");
        _permissiveValues.add("designated");
		_permissiveValues.add("official");
		_permissiveValues.add("permissive");
	}

	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		// extract profiles from GraphHopper instance
		EncodingManager encMgr = graphhopper.getEncodingManager();
		List<FlagEncoder> encoders = encMgr.fetchEdgeEncoders();
		int[] profileTypes = new int[encoders.size()];
		int i = 0;
		for (FlagEncoder enc : encoders)
		{
			profileTypes[i] = RoutingProfileType.getFromEncoderName(enc.toString());
			i++;
		}

		_storage = new AccessRestrictionsGraphStorage(profileTypes);

		return _storage;
	}

	public void processWay(ReaderWay way) {

		if (_hasRestrictions)
		{
			_hasRestrictions = false;

			_restrictions[0] = 0;
			_restrictions[1] = 0;
			_restrictions[2] = 0;
			_restrictions[3] = 0;
		}

		if (way.hasTag(_accessRestrictedTags, _restrictedValues))
		{
			_hasRestrictions = true;

			_restrictions[0] = isAccessAllowed(way, _motorCarTags) ? 0 : getRestrictionType(way, _motorCarTags);
			_restrictions[1] = isAccessAllowed(way, _motorCycleTags) ? 0 : getRestrictionType(way, _motorCycleTags);
			_restrictions[2] = isAccessAllowed(way, "bicycle") ? 0 : getRestrictionType(way, "bicycle");
			_restrictions[3] = isAccessAllowed(way, "foot") ? 0 : getRestrictionType(way, "foot");
		}
		
		if (!_hasRestrictions)
		{
			// way 316156033
			if (way.hasTag("foot", _permissiveValues) || way.hasTag("bicycle", _permissiveValues))
			{
				_hasRestrictions = true;

				_restrictions[0] = !isAccessAllowed(way, _motorCarTags) ? 0 : getRestrictionType(way, _motorCarTags);
				if (_restrictions[0] == 0)
					_restrictions[0] = AccessRestrictionType.No;
				_restrictions[1] = !isAccessAllowed(way, _motorCycleTags) ? 0 : getRestrictionType(way, _motorCycleTags);
				if (_restrictions[1] == 0)
					_restrictions[1] = AccessRestrictionType.No;
			}
		}
	}

	private int getRestrictionType(ReaderWay way, List<String> tags)
	{
		int res = 0;

		String tagValue = way.getTag("access");
		if (tagValue != null && tagValue.equals("customers"))
			res |= AccessRestrictionType.Customers;

		if (tags != null)
		{
			for (String key : tags)
			{
				tagValue = way.getTag(key);
				if (tagValue != null)
				{
					if (tagValue.equals("no"))
						res |= AccessRestrictionType.No;
					if (tagValue.equals("destination"))
						res |= AccessRestrictionType.Destination;
				}
			}
		}

		return res;
	}

	private int getRestrictionType(ReaderWay way, String tag)
	{
		int res = 0;

		String tagValue = way.getTag("access");
		if (tagValue != null && tagValue.equals("customers"))
			res |= AccessRestrictionType.Customers;

		tagValue = way.getTag(tag);
		if (tagValue != null)
		{
			if (tagValue.equals("no"))
				res |= AccessRestrictionType.No;
			if (tagValue.equals("destination"))
				res |= AccessRestrictionType.Destination;
		}

		return res;
	}

	private boolean isAccessAllowed(ReaderWay way, List<String> tagNames)
	{
		return way.hasTag(tagNames, _permissiveValues);
	}

	private boolean isAccessAllowed(ReaderWay way, String tagName)
	{
		return way.hasTag(tagName, _permissiveValues);
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge)
	{
		if (_hasRestrictions) 
			_storage.setEdgeValue(edge.getEdge(), _restrictions);
	}

	@Override
	public String getName() {
		return "AccessRestrictions";
	}
}
