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
package org.heigit.ors.fastisochrones.partitioning;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.StorableProperties;
import com.graphhopper.util.Helper;
import org.heigit.ors.config.IsochronesServiceSettings;
import org.heigit.ors.fastisochrones.partitioning.storage.CellStorage;
import org.heigit.ors.fastisochrones.partitioning.storage.IsochroneNodeStorage;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopperConfig;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSParameters.FastIsochrone;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.heigit.ors.fastisochrones.partitioning.FastIsochroneParameters.*;

/**
 * Factory for Fast Isochrone Preparation
 * <p>
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author Hendrik Leuschner
 */
public class FastIsochroneFactory {
    private List<Profile> fastIsochroneProfiles;
    private PreparePartition partition;
    private boolean disablingAllowed = true;
    private boolean enabled = false;
    private IsochroneNodeStorage isochroneNodeStorage;
    private CellStorage cellStorage;


    public void init(GraphHopperConfig ghConfig) {
        ORSGraphHopperConfig orsConfig = (ORSGraphHopperConfig) ghConfig;
        setMaxThreadCount(orsConfig.getInt(FastIsochrone.PREPARE + "threads", getMaxThreadCount()));
        setMaxCellNodesNumber(orsConfig.getInt(FastIsochrone.PREPARE + "maxcellnodes", getMaxCellNodesNumber()));
        fastIsochroneProfiles = orsConfig.getFastisochroneProfiles();
        boolean enableThis = !fastIsochroneProfiles.isEmpty();
        setEnabled(enableThis);
        if (enableThis) {
            setDisablingAllowed(orsConfig.getBool(FastIsochrone.INIT_DISABLING_ALLOWED, isDisablingAllowed()));
            IsochronesServiceSettings.setFastIsochronesActive(orsConfig.getString(FastIsochrone.PROFILE, ""));
        }
    }

    public List<Profile> getFastIsochroneProfiles() {
        return fastIsochroneProfiles;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables core calculation..
     */
    public final FastIsochroneFactory setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public final boolean isDisablingAllowed() {
        return disablingAllowed || !isEnabled();
    }

    /**
     * This method specifies if it is allowed to disable Core routing at runtime via routing hints.
     */
    public final FastIsochroneFactory setDisablingAllowed(boolean disablingAllowed) {
        this.disablingAllowed = disablingAllowed;
        return this;
    }

    public FastIsochroneFactory setPartition(PreparePartition pp) {
        partition = pp;
        return this;
    }

    public PreparePartition getPartition() {
        return partition;
    }



    public void prepare(final StorableProperties properties) {
        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(threadPool);
        final String name = "PreparePartition";
        completionService.submit(() -> {
            // toString is not taken into account so we need to cheat, see http://stackoverflow.com/q/6113746/194609 for other options
            Thread.currentThread().setName(name);
            getPartition().prepare();
            setIsochroneNodeStorage(getPartition().getIsochroneNodeStorage());
            setCellStorage(getPartition().getCellStorage());
            properties.put(FastIsochrone.PREPARE + "date." + name, Helper.createFormatter().format(new Date()));
        }, name);

        threadPool.shutdown();

        try {
            completionService.take().get();
        } catch (Exception e) {
            threadPool.shutdownNow();
            throw new IllegalStateException(e);
        }
    }

    public void createPreparation(GraphHopperStorage ghStorage, EdgeFilterSequence edgeFilters) {
        if (!isEnabled() || (partition != null))
            return;
        PreparePartition tmpPreparePartition = new PreparePartition(ghStorage, edgeFilters);
        setPartition(tmpPreparePartition);
    }

    public void setExistingStorages() {
        setIsochroneNodeStorage(getPartition().getIsochroneNodeStorage());
        setCellStorage(getPartition().getCellStorage());
    }

    public IsochroneNodeStorage getIsochroneNodeStorage() {
        return isochroneNodeStorage;
    }

    public void setIsochroneNodeStorage(IsochroneNodeStorage isochroneNodeStorage) {
        this.isochroneNodeStorage = isochroneNodeStorage;
    }

    public CellStorage getCellStorage() {
        return cellStorage;
    }

    public void setCellStorage(CellStorage cellStorage) {
        this.cellStorage = cellStorage;
    }

    public long getCapacity() {
        return cellStorage.getCapacity() + isochroneNodeStorage.getCapacity();
    }
}
