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
package org.heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ORSGraphHopperStorage extends GraphHopperStorage {
    private final Collection<CHEntry> coreEntries;

    public ORSGraphHopperStorage(Directory dir, EncodingManager encodingManager, boolean withElevation, boolean withTurnCosts, int segmentSize) {
        super(dir, encodingManager, withElevation, withTurnCosts, segmentSize);
        coreEntries = new ArrayList<>();
    }

    /**
     * Adds a {@link CHStorage} for the given {@link CHConfig}. You need to call this method before calling {@link #create(long)}
     * or {@link #loadExisting()}.
     */
    public ORSGraphHopperStorage addCoreGraph(CHConfig chConfig) {
        if (getCoreConfigs().contains(chConfig))
            throw new IllegalArgumentException("For the given CH profile a CHStorage already exists: '" + chConfig.getName() + "'");
        coreEntries.add(createCHEntry(chConfig));
        return this;
    }

    /**
     * @see #addCHGraph(CHConfig)
     */
    public ORSGraphHopperStorage addCoreGraphs(List<CHConfig> chConfigs) {
        for (CHConfig chConfig : chConfigs) {
            addCoreGraph(chConfig);
        }
        return this;
    }

    /**
     * @return the {@link CHStorage} for the specified profile name, or null if it does not exist
     */
    public CHStorage getCoreStore(String chName) {
        CHEntry chEntry = getCoreEntry(chName);
        return chEntry == null ? null : chEntry.chStore;
    }

    /**
     * @return the {@link RoutingCHGraph} for the specified profile name, or null if it does not exist
     */
    public RoutingCHGraph getCoreGraph(String chName) {
        CHEntry chEntry = getCoreEntry(chName);
        return chEntry == null ? null : chEntry.chGraph;
    }

    public CHEntry getCoreEntry(String chName) {
        for (CHEntry cg : coreEntries) {
            if (cg.chConfig.getName().equals(chName))
                return cg;
        }
        return null;
    }

    public List<String> getCoreGraphNames() {
        return coreEntries.stream().map(ch -> ch.chConfig.getName()).collect(Collectors.toList());
    }

    public List<CHConfig> getCoreConfigs() {
        return coreEntries.stream().map(c -> c.chConfig).collect(Collectors.toList());
    }

    /**
     * After configuring this storage you need to create it explicitly.
     */
    public ORSGraphHopperStorage create(long byteCount) {
        super.create(byteCount);

        coreEntries.forEach(ch -> ch.chStore.create());

        List<CHConfig> coreConfigs = getCoreConfigs();
        List<String> coreProfileNames = new ArrayList<>(coreConfigs.size());
        for (CHConfig chConfig : coreConfigs) {
            coreProfileNames.add(chConfig.getName());
        }
        getProperties().put("graph.core.profiles", coreProfileNames.toString());
        return this;
    }

    @Override
    public void loadExistingORS() {
            coreEntries.forEach(cg -> {
                if (!cg.chStore.loadExisting())
                    throw new IllegalStateException("Cannot load " + cg);
            });
            if (getExtensions() != null) {
                getExtensions().loadExisting();
            }
    }

    public void flush() {
        super.flush();
        coreEntries.stream().map(ch -> ch.chStore).filter(s -> !s.isClosed()).forEach(CHStorage::flush);
    }

    @Override
    public void close() {
        super.close();
        coreEntries.stream().map(ch -> ch.chStore).filter(s -> !s.isClosed()).forEach(CHStorage::close);
    }

    @Override
    public long getCapacity() {
        return super.getCapacity() + coreEntries.stream().mapToLong(ch -> ch.chStore.getCapacity()).sum();
    }

    /**
     * Avoid that edges and nodes of the base graph are further modified. Necessary as hook for e.g.
     * ch graphs on top to initialize themselves
     */
    public synchronized void freeze() {
        if (isFrozen())
            return;
        super.freeze();
        coreEntries.forEach(ch -> {
            // we use a rather small value here. this might result in more allocations later, but they should
            // not matter that much. if we expect a too large value the shortcuts DataAccess will end up
            // larger than needed, because we do not do something like trimToSize in the end.
            double expectedShortcuts = 0.3 * getBaseGraph().getEdges();
            ch.chStore.init(getBaseGraph().getNodes(), (int) expectedShortcuts);
        });
    }

    @Override
    public String toDetailsString() {
        String str = super.toDetailsString();
        for (CHEntry ch : coreEntries) {
            str += ", " + ch.chStore.toDetailsString();
        }

        return str;
    }

    // estimated number of core nodes used for array initialization in Tarjan
    public int getCoreNodes() {
        for (CHEntry cg : coreEntries) {
            if (cg.chGraph.getCoreNodes() == -1) continue;
            return cg.chGraph.getCoreNodes();
        }
        throw new IllegalStateException("No prepared core graph was found");
    }
}
