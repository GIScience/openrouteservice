package org.heigit.ors.fastisochrones;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraphImpl;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import org.heigit.ors.partitioning.CellStorage;
import org.heigit.ors.partitioning.IsochroneNodeStorage;
import org.heigit.ors.partitioning.EccentricityStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static heigit.ors.partitioning.FastIsochroneParameters.FASTISO_MAXTHREADCOUNT;


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

    protected ExecutorService threadPool;



    public AbstractEccentricity(GraphHopperStorage ghStorage){
        this.ghStorage = ghStorage;
        this.threadPool = java.util.concurrent.Executors.newFixedThreadPool(Math.min(FASTISO_MAXTHREADCOUNT, Runtime.getRuntime().availableProcessors()));
    }


    public abstract void calcEccentricities();

    public EccentricityStorage getEccentricityStorage(Weighting weighting){
        if (eccentricityStorages.isEmpty())
            return null;
        for(EccentricityStorage ecc : eccentricityStorages){
            if(ecc.getWeighting().getName() == weighting.getName() && ecc.getWeighting().getFlagEncoder().toString() == weighting.getFlagEncoder().toString())
                return ecc;
        }
        return null;
    }

    public boolean loadExisting(Weighting weighting){
        EccentricityStorage eccentricityStorage = new EccentricityStorage(ghStorage, ghStorage.getDirectory(), weighting);
        eccentricityStorages.add(eccentricityStorage);
        return eccentricityStorage.loadExisting();
    }

    public AbstractEccentricity setGraph(Graph _baseGraph) {
        this.baseGraph = _baseGraph;
        return this;
    }

    public AbstractEccentricity setCHGraph(CHGraphImpl _chGraph) {
        this.chGraph = _chGraph;
        return this;
    }

    public AbstractEccentricity setWeighting(Weighting _weighting) {
        this.weighting = _weighting;
        return this;
    }

    public AbstractEccentricity setEncoder(FlagEncoder _encoder) {
        this.encoder = _encoder;
        return this;
    }

}
