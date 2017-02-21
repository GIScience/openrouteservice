/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov 

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
}
