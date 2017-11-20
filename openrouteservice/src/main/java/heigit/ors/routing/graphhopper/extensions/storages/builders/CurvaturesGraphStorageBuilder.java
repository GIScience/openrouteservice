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
    private double _sinousity;
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
     * Returns the sinousity factor determined from
     * flight distance divided distance of the way
     * <p>
     * This method always returns between 0 and 1 with
     * smaller values indicating windy ways and
     * higher values indicating straight ways
     *
     * @param edge edge object from originating from EdgeIteratorState
     * @return the sinousity factor
     */
    private double computeSinousity(EdgeIteratorState edge) {

        distance = edge.getDistance();
        pl = edge.fetchWayGeometry(3);
        startLat = pl.getLat(0);
        startLng = pl.getLon(0);
        endLat = pl.getLat(pl.getSize() - 1);
        endLng = pl.getLon(pl.getSize() - 1);
        flightDistance = distCalc.calcDist(startLat, startLng, endLat, endLng);

        return flightDistance / distance;


    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {

        //edge.getEdge();

        _sinousity = computeSinousity(edge);

        _storage.setEdgeValue(edge.getEdge(), _sinousity);
    }

    @Override
    public String getName() {
        return "Curvatures";
    }

}






