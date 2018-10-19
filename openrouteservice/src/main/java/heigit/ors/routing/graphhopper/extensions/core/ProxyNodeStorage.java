///*
// *  Licensed to GraphHopper GmbH under one or more contributor
// *  license agreements. See the NOTICE file distributed with this work for
// *  additional information regarding copyright ownership.
// *
// *  GraphHopper GmbH licenses this file to you under the Apache License,
// *  Version 2.0 (the "License"); you may not use this file except in
// *  compliance with the License. You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//package heigit.ors.routing.graphhopper.extensions.core;
//
//import com.carrotsearch.hppc.IntArrayList;
//import com.carrotsearch.hppc.IntHashSet;
//import com.carrotsearch.hppc.IntObjectMap;
//import com.carrotsearch.hppc.predicates.IntObjectPredicate;
//import com.carrotsearch.hppc.procedures.IntObjectProcedure;
//import com.graphhopper.coll.MapEntry;
//import com.graphhopper.routing.DijkstraBidirectionRef;
//import com.graphhopper.routing.lm.LandmarkStorage;
//import com.graphhopper.routing.lm.LandmarkSuggestion;
//import com.graphhopper.routing.subnetwork.SubnetworkStorage;
//import com.graphhopper.routing.util.AllEdgesIterator;
//import com.graphhopper.routing.util.EdgeFilter;
//import com.graphhopper.routing.util.FlagEncoder;
//import com.graphhopper.routing.util.TraversalMode;
//import com.graphhopper.routing.util.spatialrules.SpatialRule;
//import com.graphhopper.routing.util.spatialrules.SpatialRuleLookup;
//import com.graphhopper.routing.weighting.AbstractWeighting;
//import com.graphhopper.routing.weighting.ShortestWeighting;
//import com.graphhopper.routing.weighting.Weighting;
//import com.graphhopper.storage.*;
//import com.graphhopper.util.*;
//import com.graphhopper.util.exceptions.ConnectionNotFoundException;
//import com.graphhopper.util.shapes.BBox;
//import com.graphhopper.util.shapes.GHPoint;
//import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * This class stores the landmark nodes and the weights from and to all other nodes in every
// * subnetwork. This data is created to apply a speed-up for path calculation but at the same times
// * stays flexible to per-request changes. The class is safe for usage from multiple reading threads
// * across algorithms.
// *
// * @author Peter Karich
// */
//public class ProxyNodeStorage implements Storable<ProxyNodeStorage>{
//
//    private final DataAccess proxyNodes;
//    private int PROXYBYTES;
//    private int PROXY_OFFSET;
//
//    /**
//     * 'to' and 'from' fit into 32 bit => 16 bit for each of them => 65536
//     */
//
//    public ProxyNodeStorage(GraphHopperStorage graph, Directory dir, final Weighting weighting, int landmarks) {
//
//
//        //TODO Add specificier for landmark definition
//        proxyNodes = dir.find("proxy_nodes_" + AbstractWeighting.weightingToFileName(weighting));
//        proxyNodes.create(1000);
//        // 4 byte for the proxy id and 4 byte for the to distance to it and 4 byte for the from distance
//        this.PROXYBYTES = 16;
//        this.PROXY_OFFSET = 4;
//    }
//
//
//    @Override
//    public boolean loadExisting() {
//        if (proxyNodes.loadExisting()) {
//            return true;
//        }
//        return false;
//    }
//
//
//    public boolean isEmpty() {
//        return landmarkIDs.size() < 2;
//    }
//
//
//    @Override
//    public ProxyNodeStorage create(long byteCount) {
//        throw new IllegalStateException("Do not call LandmarkStore.create directly");
//    }
//
//    @Override
//    public void flush() {
//        proxyNodes.flush();
//    }
//
//    @Override
//    public void close() {
//        proxyNodes.close();
//
//    }
//
//    @Override
//    public boolean isClosed() {
//        return landmarkWeightDA.isClosed();
//    }
//
//    public long getCapacity() {
//        return landmarkWeightDA.getCapacity() + subnetworkStorage.getCapacity();
//    }
//
//
//}
