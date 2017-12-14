/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import heigit.ors.routing.graphhopper.extensions.storages.CurvaturesGraphStorage;

public class CurvaturesGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private CurvaturesGraphStorage _storage;
    private double _sinuosity;
    private PointList pl;
    private double distance;
    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;
    private double flightDistance;
    private final DistanceCalc distCalc = Helper.DIST_EARTH;

    public CurvaturesGraphStorageBuilder() {
    }

    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        _storage = new CurvaturesGraphStorage();

        return _storage;
    }

    public void processWay(ReaderWay way) {

    }

    /**
     * Returns the sinuosity factor determined from
     * flight distance divided distance of the way
     * <p>
     * This method always returns between 0 and 1 with
     * smaller values indicating windy ways and
     * higher values indicating straight ways
     *
     * @param edge edge object from originating from EdgeIteratorState
     * @return the sinuosity factor
     */
    private double computeSinuosity(EdgeIteratorState edge) {

        // distance of the edge
        distance = edge.getDistance();      // what happens if start and end point are the same, division by zero??
        // create pointlist of points on the edge
        pl = edge.fetchWayGeometry(3);
        // get latitude and longitude of starting point of the edge (first element in the pointlist)
        startLat = pl.getLat(0);
        startLng = pl.getLon(0);
        // get latitude and longitude of end point of the edge (last element in the pointlist)
        endLat = pl.getLat(pl.getSize() - 1);
        endLng = pl.getLon(pl.getSize() - 1);
        // calculate flight distance from start and end point of the edge
        flightDistance = distCalc.calcDist(startLat, startLng, endLat, endLng);

        // return sinuosity of the edge
        return flightDistance / distance;


    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {

        //edge.getEdge();

        // calculate sinuosity of edge
        _sinuosity = computeSinuosity(edge);

        // push sinuosity value to storage
        _storage.setEdgeValue(edge.getEdge(), _sinuosity);
    }

    @Override
    public String getName() {
        return "Curvatures";
    }

}






