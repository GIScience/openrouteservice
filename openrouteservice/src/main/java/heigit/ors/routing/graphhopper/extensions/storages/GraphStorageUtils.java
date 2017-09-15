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
package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;

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
