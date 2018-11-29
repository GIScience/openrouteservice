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
package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.ExtendedStorageSequence;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;

import java.util.List;

public class GraphStorageUtils {

	@SuppressWarnings("unchecked")
	public static <T extends GraphExtension> T getGraphExtension(GraphStorage graphStorage, Class<T> type)
	{
		if (graphStorage != null) {
			if (graphStorage instanceof GraphHopperStorage) {
				GraphHopperStorage ghs = (GraphHopperStorage) graphStorage;
				GraphExtension ge = ghs.getExtension();

				if(ge instanceof ExtendedStorageSequence)
				{
					ExtendedStorageSequence ess = (ExtendedStorageSequence)ge;
					GraphExtension[] exts = ess.getExtensions();
					for (int i = 0; i < exts.length; i++)
					{
						if (type.isInstance(exts[i])) {
							return (T)exts[i];
						}
					}
				}
				else 
				{
					if (type.isInstance(ge)) {
						return (T)ge;
					}
				}
			}
		}

		return null;
	}

	public static GraphExtension[] getGraphExtensions(GraphStorage graphStorage) {
		if(graphStorage != null) {
			if(graphStorage instanceof GraphHopperStorage) {
				GraphHopperStorage ghs = (GraphHopperStorage) graphStorage;
				GraphExtension ge = ghs.getExtension();

				if(ge instanceof  ExtendedStorageSequence) {
					ExtendedStorageSequence ess = (ExtendedStorageSequence)ge;
					return ess.getExtensions();
				} else {
					return new GraphExtension[] {ge};
				}

			}
		}
		return null;
	}
	
	public static long getCapacity(GraphExtension ext)
	{
		if (!(ext instanceof GraphExtension.NoOpExtension))
    	{
			long capacity = 0;
			
    		if(ext instanceof ExtendedStorageSequence)
			{
				ExtendedStorageSequence ess = (ExtendedStorageSequence)ext;
				GraphExtension[] exts = ess.getExtensions();
				for (int i = 0; i < exts.length; i++)
				{
					capacity += exts[i].getCapacity();
				}
			}
			else 
			{
				capacity += ext.getCapacity();
			}
    		
    		return capacity;
    	}
		
		return 0;
	}
}
