package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.fastisochrones.storage.BorderNodeDistanceStorage;
import org.heigit.ors.fastisochrones.storage.EccentricityStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass for eccentricity calculations. Stores and orders references to eccentricity and bordernodedistance storages.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public abstract class AbstractEccentricity {
    protected GraphHopperStorage ghStorage;
    protected IsochroneNodeStorage isochroneNodeStorage;
    protected CellStorage cellStorage;
    protected List<EccentricityStorage> eccentricityStorages = new ArrayList<>();
    protected List<BorderNodeDistanceStorage> borderNodeDistanceStorages = new ArrayList<>();

    protected AbstractEccentricity(GraphHopperStorage ghStorage) {
        this.ghStorage = ghStorage;
    }

    public abstract void calcEccentricities(Weighting weighting, EdgeFilter additionalEdgeFilter, FlagEncoder flagEncoder);

    public EccentricityStorage getEccentricityStorage(Weighting weighting) {
        if (eccentricityStorages.isEmpty())
            return null;
        for (EccentricityStorage ecc : eccentricityStorages) {
            if (ecc.hasWeighting(weighting))
                return ecc;
        }
        return null;
    }

    public BorderNodeDistanceStorage getBorderNodeDistanceStorage(Weighting weighting) {
        if (borderNodeDistanceStorages.isEmpty())
            return null;
        for (BorderNodeDistanceStorage bnds : borderNodeDistanceStorages) {
            if (bnds.hasWeighting(weighting))
                return bnds;
        }
        return null;
    }

    public boolean loadExisting(Weighting weighting) {
        EccentricityStorage eccentricityStorage = new EccentricityStorage(ghStorage.getDirectory(), weighting, isochroneNodeStorage, ghStorage.getBaseGraph().getNodes());
        eccentricityStorages.add(eccentricityStorage);

        BorderNodeDistanceStorage borderNodeDistanceStorage = new BorderNodeDistanceStorage(ghStorage.getDirectory(), weighting, isochroneNodeStorage, ghStorage.getBaseGraph().getNodes());
        borderNodeDistanceStorages.add(borderNodeDistanceStorage);
        borderNodeDistanceStorage.loadExisting();

        return eccentricityStorage.loadExisting();
    }

    public boolean isAvailable(Weighting weighting) {
        return getEccentricityStorage(weighting) != null;
    }
}
