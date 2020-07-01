package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraphImpl;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.BorderNodeDistanceStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.EccentricityStorage;

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
    protected Graph baseGraph;
    protected Weighting weighting;
    protected FlagEncoder encoder;
    protected CHGraphImpl chGraph;
    protected TraversalMode traversalMode;
    protected IsochroneNodeStorage isochroneNodeStorage;
    protected CellStorage cellStorage;
    protected List<EccentricityStorage> eccentricityStorages = new ArrayList<>();
    protected List<BorderNodeDistanceStorage> borderNodeDistanceStorages = new ArrayList<>();

    public AbstractEccentricity(GraphHopperStorage ghStorage) {
        this.ghStorage = ghStorage;
    }

    public abstract void calcEccentricities();

    public EccentricityStorage getEccentricityStorage(Weighting weighting) {
        if (eccentricityStorages.isEmpty())
            return null;
        for (EccentricityStorage ecc : eccentricityStorages) {
            if (ecc.getWeighting().getName() != null && ecc.getWeighting().getName().equals(weighting.getName())
                    && ecc.getWeighting().getFlagEncoder().toString() != null && ecc.getWeighting().getFlagEncoder().toString().equals(weighting.getFlagEncoder().toString()))
                return ecc;
        }
        return null;
    }

    public BorderNodeDistanceStorage getBorderNodeDistanceStorage(Weighting weighting) {
        if (borderNodeDistanceStorages.isEmpty())
            return null;
        for (BorderNodeDistanceStorage bnds : borderNodeDistanceStorages) {
            if (bnds.getWeighting().getName() != null && bnds.getWeighting().getName().equals(weighting.getName())
                    && bnds.getWeighting().getFlagEncoder().toString() != null && bnds.getWeighting().getFlagEncoder().toString().equals(weighting.getFlagEncoder().toString()))
                return bnds;
        }
        return null;
    }

    public boolean loadExisting(Weighting weighting) {
        EccentricityStorage eccentricityStorage = new EccentricityStorage(ghStorage, ghStorage.getDirectory(), weighting, isochroneNodeStorage);
        eccentricityStorages.add(eccentricityStorage);

        BorderNodeDistanceStorage borderNodeDistanceStorage = new BorderNodeDistanceStorage(ghStorage, ghStorage.getDirectory(), weighting, isochroneNodeStorage);
        borderNodeDistanceStorages.add(borderNodeDistanceStorage);
        borderNodeDistanceStorage.loadExisting();

        return eccentricityStorage.loadExisting();
    }

    public AbstractEccentricity setGraph(Graph baseGraph) {
        this.baseGraph = baseGraph;
        return this;
    }

    public AbstractEccentricity setCHGraph(CHGraphImpl chGraph) {
        this.chGraph = chGraph;
        return this;
    }

    public AbstractEccentricity setWeighting(Weighting weighting) {
        this.weighting = weighting;
        return this;
    }

    public AbstractEccentricity setEncoder(FlagEncoder flagEncoder) {
        this.encoder = flagEncoder;
        return this;
    }
}
