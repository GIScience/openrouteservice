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
package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HillIndexGraphStorage;

public class SteepnessDifficultyWeighting extends FastestWeighting
{
    /**
     * For now used only in BikeCommonFlagEncoder and MotorcycleFlagEncoder
     */
    public static final int KEY = 101;
    
	private HillIndexGraphStorage gsHillIndex;
	private byte[] buffer;
	private double[] difficultyWeights;
	
	private static double BIKE_DIFFICULTY_MATRIX[][];
	//private static double HIKE_DIFFICULTY_MATRIX[][];
	
	static 
	{
		BIKE_DIFFICULTY_MATRIX = new double[4][20];
		//HIKE_DIFFICULTY_MATRIX = new double[4][20];
		
		for (int d = 0; d <= 3; d++)
		{
			double[] bikeDifficultyWeights = BIKE_DIFFICULTY_MATRIX[d];
			//double[] hikeDifficultyWeights = HIKE_DIFFICULTY_MATRIX[d];

			for (int i = 0; i < 20; i++)
			{
				if (d == 0)
				{
					// BIKE 
					if (i <= 0)
						bikeDifficultyWeights[i] = 0.5;
					else if(i == 1)
						bikeDifficultyWeights[i] = 0.5;
					else if (i == 2)
						bikeDifficultyWeights[i] = 0.5;
					else if (i == 3)
						bikeDifficultyWeights[i] = 0.7;
					else if (i == 4)
						bikeDifficultyWeights[i] = 0.9;
					else if (i == 5)
						bikeDifficultyWeights[i] = 1.5;
					else if (i == 6)
						bikeDifficultyWeights[i] = 3;
					else if (i == 7)
						bikeDifficultyWeights[i] = 3.5;
					else if (i == 8)
						bikeDifficultyWeights[i] = 4;
					else if (i == 9)
						bikeDifficultyWeights[i] = 5;
					else //if (i >= 6)
						bikeDifficultyWeights[i] = 6 + 0.5*i;

					// HIKE
				}
				else if (d == 1)
				{
					if (i <= 0)
						bikeDifficultyWeights[i] = 0.7;
					else if(i == 1)
						bikeDifficultyWeights[i] = 0.6;
					else if (i == 2)
						bikeDifficultyWeights[i] = 0.6;
					else if (i == 3)
						bikeDifficultyWeights[i] = 0.5;  // prefer
					else if (i == 4)
						bikeDifficultyWeights[i] = 0.5;  // prefer
					else if (i == 5)
						bikeDifficultyWeights[i] = 0.8;
					else if (i == 6)
						bikeDifficultyWeights[i] = 1.0;
					else if (i == 7)
						bikeDifficultyWeights[i] = 2;
					else if (i == 8)
						bikeDifficultyWeights[i] = 3;
					else if (i == 9)
						bikeDifficultyWeights[i] = 4;
					else if (i == 10)
						bikeDifficultyWeights[i] = 5;
					else 
						bikeDifficultyWeights[i] = 6 + 0.5*i;
					
					// HIKE
				}
				else if (d ==2)
				{
					if (i <= 0)
						bikeDifficultyWeights[i] = 1.6;
					else if(i == 1)
						bikeDifficultyWeights[i] = 1.6;
					else if (i == 2)
						bikeDifficultyWeights[i] = 1.5;
					else if (i == 3)
						bikeDifficultyWeights[i] = 1.5;
					else if (i == 4)
						bikeDifficultyWeights[i] = 0.7;
					else if (i == 5)
						bikeDifficultyWeights[i] = 0.5;
					else if (i == 6)
						bikeDifficultyWeights[i] = 0.5;  // prefer
					else if (i == 7)
						bikeDifficultyWeights[i] = 0.5;    // prefer
					else if (i == 8)
						bikeDifficultyWeights[i] = 1;   // prefer
					else if (i == 9)
						bikeDifficultyWeights[i] = 2;
					else if (i == 10)
						bikeDifficultyWeights[i] = 2.5;
					else if (i == 11)
						bikeDifficultyWeights[i] = 2.5;
					else if (i == 12)
						bikeDifficultyWeights[i] = 3;
					else if (i == 13)
						bikeDifficultyWeights[i] = 4;
					else if (i <= 16)
						bikeDifficultyWeights[i] = 5;
					else 
						bikeDifficultyWeights[i] = 6 + 0.1*i;
					
					// HIKE
				}
				else if (d ==3)
				{
					if (i <= 0)
						bikeDifficultyWeights[i] = 1.6;
					else if(i == 1)
						bikeDifficultyWeights[i] = 1.6;
					else if (i == 2)
						bikeDifficultyWeights[i] = 1.5;
					else if (i == 3)
						bikeDifficultyWeights[i] = 1.5;
					else if (i == 4)
						bikeDifficultyWeights[i] = 0.9;
					else if (i == 5)
						bikeDifficultyWeights[i] = 0.7;
					else if (i == 6)
						bikeDifficultyWeights[i] = 0.5;  // prefer
					else if (i == 7)
						bikeDifficultyWeights[i] = 0.5;    // prefer
					else if (i == 8)
						bikeDifficultyWeights[i] = 0.6;   // prefer
					else if (i == 9)
						bikeDifficultyWeights[i] = 0.7;
					else if (i == 10)
						bikeDifficultyWeights[i] = 0.9;
					else if (i == 11)
						bikeDifficultyWeights[i] = 1.2;
					else if (i == 12)
						bikeDifficultyWeights[i] = 2;
					else if (i == 13)
						bikeDifficultyWeights[i] = 3;
					else if (i == 14)
						bikeDifficultyWeights[i] = 5;
					else if (i <= 16)
						bikeDifficultyWeights[i] = 6;
					else 
						bikeDifficultyWeights[i] = 6 + 0.1*i;
					
					// HIKE
				}
			}
			
			// TODO create its own weights for hike/pedestrian profiles
			//hikeDifficultyWeights = bikeDifficultyWeights;
		}
	}

    public SteepnessDifficultyWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage)
    {
        super(encoder, map);
        
        buffer = new byte[1];

	    int difficultyLevel = map.getInt("level", -1);

        gsHillIndex = GraphStorageUtils.getGraphExtension(graphStorage, HillIndexGraphStorage.class);
        
        if (gsHillIndex != null)
        {
        	if (difficultyLevel >= 0)
        	{
        		//String name = encoder.toString();
        		/*if (name.equals("hike") || name.equals("hike2") || name.equals("foot"))
        			difficultyWeights = HIKE_DIFFICULTY_MATRIX[difficultyLevel];
        		else*/
        			difficultyWeights = BIKE_DIFFICULTY_MATRIX[difficultyLevel];
        	}
        }
    }
    
    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId )
    {
    	if (gsHillIndex != null)
    	{
    		boolean revert = edgeState.getBaseNode() < edgeState.getAdjNode();
    		int hillIndex = gsHillIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), revert, buffer);

    		if (difficultyWeights != null)
    			return difficultyWeights[hillIndex];
    	}

   		return 1.0;
    }
}
