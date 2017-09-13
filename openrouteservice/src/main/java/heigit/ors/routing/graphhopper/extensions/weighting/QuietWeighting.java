/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.NoiseIndexGraphStorage;

public class QuietWeighting extends FastestWeighting {
    private NoiseIndexGraphStorage _gsNoiseIndex;
    private byte[] _buffer;
    private double _weightingFactor = 1;

    public QuietWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
        super(encoder, map);
        _buffer = new byte[1];
        _gsNoiseIndex = GraphStorageUtils.getGraphExtension(graphStorage, NoiseIndexGraphStorage.class);
        _weightingFactor = map.getDouble("factor", 1);
    }

    private double calcNoiseWeightFactor(int level) {
    	if ( level == 0)
    		return 1;
        else if ( level <=1 )
        	return 1 + _weightingFactor * 10;
        else if ( level <=2 )
        	return 1 + _weightingFactor * _weightingFactor * 200;  // drop factor for noise level 2 and 3 dramatically, but still larger then the factor for noise level 1 
        else if (level <=3 )
        	return 1 + _weightingFactor * _weightingFactor * 400;
        else
        	throw new AssertionError("The noise level "+  level + " is not supported!");
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if (_gsNoiseIndex != null) {
            int noiseLevel = _gsNoiseIndex.getEdgeValue(edgeState.getOriginalEdge(), _buffer);
            return calcNoiseWeightFactor(noiseLevel);
        }

        return 1.0;
    }
}