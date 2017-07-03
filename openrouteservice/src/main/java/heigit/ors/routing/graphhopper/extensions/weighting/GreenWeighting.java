package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.GreenIndexGraphStorage;

/**
 * Created by lliu on 15/03/2017.
 */
public class GreenWeighting extends FastestWeighting {
    private Weighting _superWeighting;
    private GreenIndexGraphStorage _gsGreenIndex;
    private int _totalLevel = 64;
    private byte[] _buffer;

    public GreenWeighting(Weighting superWeighting, FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
        super(encoder, map);
        this._superWeighting = superWeighting;
        _buffer = new byte[1];
        _gsGreenIndex = GraphStorageUtils.getGraphExtension(graphStorage, GreenIndexGraphStorage.class);
        // TODO 			double greenWeightingFactor = hintsMap.getDouble("green_weighting_factor", 1);
    }

    private double calcGreenWeightFactor(int level) {
        // There is an implicit convention here:
        // the level range is [0, total - 1].
        // And the @level will be transformed to a float number
        // falling in (0, 2] linearly
        return (double) (level + 1) * 2.0 / _totalLevel;
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if (_gsGreenIndex != null) {
            int greenLevel = _gsGreenIndex.getEdgeValue(edgeState.getOriginalEdge(), _buffer);
            return _superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId) * calcGreenWeightFactor(greenLevel);
        }

        return _superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    }
}
