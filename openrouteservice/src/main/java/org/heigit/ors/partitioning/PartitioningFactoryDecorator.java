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
package org.heigit.ors.partitioning;

import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.util.ORSParameters.Partition;

import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements the Core Algo decorator and provides several helper methods related to core
 * preparation and its vehicle profiles.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class PartitioningFactoryDecorator  {
    private final List<PreparePartition> preparations = new ArrayList<>();
    private boolean disablingAllowed = true;
    private final List<Weighting> weightings = new ArrayList<>();
    private final Set<String> weightingsAsStrings = new LinkedHashSet<>();
    // for backward compatibility enable CH by default.
    private boolean enabled = true;
    private int preparationThreads;
    private ExecutorService threadPool;


    private IsochroneNodeStorage isochroneNodeStorage;
    private CellStorage cellStorage;

    public PartitioningFactoryDecorator() {
        setPreparationThreads(1);
    }

    public void init(CmdArgs args) {
        // throw explicit error for deprecated configs
        //TODO Partitioning parameters
        if (!args.get("prepare.threads", "").isEmpty())
            throw new IllegalStateException("Use " + Partition.PREPARE + "threads instead of prepare.threads");

        setPreparationThreads(args.getInt(Partition.PREPARE + "threads", getPreparationThreads()));

//        // default is enabled & fastest
//        String isoCoreWeightingsStr = args.get(Partition.PREPARE + "weightings", "");
//
//        if ("no".equals(isoCoreWeightingsStr)) {
//            // default is fastest and we need to clear this explicitely
//            weightingsAsStrings.clear();
//        } else if (!isoCoreWeightingsStr.isEmpty()) {
//            List<String> tmpCHWeightingList = Arrays.asList(isoCoreWeightingsStr.split(","));
//            setWeightingsAsStrings(tmpCHWeightingList);
//        }

        boolean enableThis = args.getBool("partitioning.enabled", false);
        setEnabled(enableThis);
        if (enableThis)
            setDisablingAllowed(args.getBool(Partition.INIT_DISABLING_ALLOWED, isDisablingAllowed()));
    }

    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables core calculation..
     */
    public final PartitioningFactoryDecorator setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public final boolean isDisablingAllowed() {
        return disablingAllowed || !isEnabled();
    }

    /**
     * This method specifies if it is allowed to disable Core routing at runtime via routing hints.
     */
    public final PartitioningFactoryDecorator setDisablingAllowed(boolean disablingAllowed) {
        this.disablingAllowed = disablingAllowed;
        return this;
    }

    public PartitioningFactoryDecorator addPreparation(PreparePartition pp) {
        preparations.add(pp);
        return this;
    }

    public List<PreparePartition> getPreparations() {
        return preparations;
    }

    public int getPreparationThreads() {
        return preparationThreads;
    }

    public PartitioningFactoryDecorator addWeighting(String weighting) {
        weightingsAsStrings.add(weighting);
        return this;
    }
    public PartitioningFactoryDecorator addWeighting(Weighting weighting) {
        weightings.add(weighting);
        return this;
    }

    public final boolean hasWeightings() {
        return !weightings.isEmpty();
    }



    /**
     * Enables the use of core to reduce query times. Enabled by default.
     *
     * @param weightingList A list containing multiple weightings like: "fastest", "shortest" or
     *                      your own weight-calculation type.
     */
    public PartitioningFactoryDecorator setWeightingsAsStrings(List<String> weightingList) {
        if (weightingList.isEmpty())
            throw new IllegalArgumentException("It is not allowed to pass an emtpy weightingList");

        weightingsAsStrings.clear();
        for (String strWeighting : weightingList) {
            strWeighting = strWeighting.toLowerCase();
            strWeighting = strWeighting.trim();
            addWeighting(strWeighting);
        }
        return this;
    }

    public List<String> getWeightingsAsStrings() {
        if (this.weightingsAsStrings.isEmpty())
            throw new IllegalStateException("Potential bug: weightingsAsStrings is empty");

        return new ArrayList<>(this.weightingsAsStrings);
    }

    private String getDefaultWeighting() {
        return weightingsAsStrings.isEmpty() ? "fastest" : weightingsAsStrings.iterator().next();
    }

    /**
     * This method changes the number of threads used for preparation on import. Default is 1. Make
     * sure that you have enough memory when increasing this number!
     */
    public void setPreparationThreads(int preparationThreads) {
        this.preparationThreads = preparationThreads;
        this.threadPool = Executors.newFixedThreadPool(preparationThreads);
    }

    public void prepare(final StorableProperties properties) {
        ExecutorCompletionService completionService = new ExecutorCompletionService<>(threadPool);
//        for (final PreparePartition prepare : getPreparations()) {
        final String name = "PreparePartition";
        completionService.submit(new Runnable() {
            @Override
            public void run() {
                // toString is not taken into account so we need to cheat, see http://stackoverflow.com/q/6113746/194609 for other options
                Thread.currentThread().setName(name);
                getPreparations().get(0).prepare();
                setIsochroneNodeStorage(getPreparations().get(0).getIsochroneNodeStorage());
                setCellStorage(getPreparations().get(0).getCellStorage());
                properties.put(Partition.PREPARE + "date." + name, Helper.createFormatter().format(new Date()));
            }
        }, name);


//        for (final PreparePartition prepare : getPreparations()) {
//            final String name = AbstractWeighting.weightingToFileName(prepare.getWeighting());
//            completionService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    // toString is not taken into account so we need to cheat, see http://stackoverflow.com/q/6113746/194609 for other options
//                    Thread.currentThread().setName(name);
//                    prepare.prepare();
//                    properties.put(ORSParameters.Partition.PREPARE + "date." + name, Helper.createFormatter().format(new Date()));
//                }
//            }, name);
//
//        }
//        }

        threadPool.shutdown();

        try {
            for (int i = 0; i < getPreparations().size(); i++) {
                completionService.take().get();
            }
        } catch (Exception e) {
            threadPool.shutdownNow();
            throw new RuntimeException(e);
        }
    }

    public void createPreparations(GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters) {
        if (!isEnabled() || !preparations.isEmpty())
            return;

//        if (weightings.isEmpty())
//            throw new IllegalStateException("No Core weightings found");

//        for (Weighting weighting : getWeightings()) {
            PreparePartition tmpPreparePartition = new PreparePartition(ghStorage, edgeFilters);
            addPreparation(tmpPreparePartition);
//        }



    }

//    public final List<Weighting> getWeightings() {
//        return weightings;
//    }


    public void setExistingStorages(){
        setIsochroneNodeStorage(getPreparations().get(0).getIsochroneNodeStorage());
        setCellStorage(getPreparations().get(0).getCellStorage());
    }

    /**
     * For now only node based will work, later on we can easily find usage of this method to remove
     * it.
     */
    public TraversalMode getNodeBase() {
        return TraversalMode.NODE_BASED;
    }


    public IsochroneNodeStorage getIsochroneNodeStorage() {
        return isochroneNodeStorage;
    }

    public CellStorage getCellStorage() {
        return cellStorage;
    }

    public void setIsochroneNodeStorage(IsochroneNodeStorage isochroneNodeStorage) {
        this.isochroneNodeStorage = isochroneNodeStorage;
    }
    public void setCellStorage(CellStorage cellStorage) {
        this.cellStorage = cellStorage;
    }
}
