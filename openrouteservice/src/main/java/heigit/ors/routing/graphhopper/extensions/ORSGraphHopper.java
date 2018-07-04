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
package heigit.ors.routing.graphhopper.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.*;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.routing.*;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import heigit.ors.routing.graphhopper.extensions.core.CoreAlgoFactoryDecorator;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidBordersCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidFeaturesCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.HeavyVehicleCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.WheelchairCoreEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.util.ORSParameters;
import heigit.ors.routing.parameters.CyclingParameters;
import heigit.ors.routing.parameters.VehicleParameters;
import heigit.ors.routing.parameters.WheelchairParameters;

public class ORSGraphHopper extends GraphHopper {

	private GraphProcessContext _procCntx;
	private HashMap<Long, ArrayList<Integer>> osmId2EdgeIds; // one osm id can correspond to multiple edges
	private HashMap<Integer, Long> tmcEdges;

	// A route profile for referencing which is used to extract names of adjacent streets and other objects.
	private RoutingProfile refRouteProfile;

	private final CoreAlgoFactoryDecorator coreFactoryDecorator = new CoreAlgoFactoryDecorator();


	public ORSGraphHopper(GraphProcessContext procCntx, boolean useTmc, RoutingProfile refProfile) {
		_procCntx = procCntx;
		this.refRouteProfile= refProfile;
		this.forDesktop();

		coreFactoryDecorator.setEnabled(true);
		algoDecorators.add(coreFactoryDecorator);

		if (useTmc){
			tmcEdges = new HashMap<Integer, Long>();
			osmId2EdgeIds = new HashMap<Long, ArrayList<Integer>>();
		}
		_procCntx.init(this);
	}

	protected DataReader createReader(GraphHopperStorage tmpGraph) {

		return initDataReader(new ORSOSMReader(tmpGraph, _procCntx, tmcEdges, osmId2EdgeIds, refRouteProfile));
	}

	public boolean load( String graphHopperFolder )
	{
		boolean res = super.load(graphHopperFolder);


		return res;
	}

	protected void flush()
	{
		super.flush();
	}

	@SuppressWarnings("unchecked")
	public GraphHopper importOrLoad() {
		GraphHopper gh = super.importOrLoad();


		if ((tmcEdges != null) && (osmId2EdgeIds !=null)) {
			java.nio.file.Path path = Paths.get(gh.getGraphHopperLocation(), "edges_ors_traffic");

			if ((tmcEdges.size() == 0) || (osmId2EdgeIds.size()==0)) {
				// try to load TMC edges from file.

				try {
					File file = path.toFile();

					if (file.exists())
					{
						FileInputStream fis = new FileInputStream(path.toString());
						ObjectInputStream ois = new ObjectInputStream(fis);
						tmcEdges = (HashMap<Integer, Long>)ois.readObject();
						osmId2EdgeIds = (HashMap<Long, ArrayList<Integer>>)ois.readObject();
						ois.close();
						fis.close();
						System.out.printf("Serialized HashMap data is saved in trafficEdges");
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				catch(ClassNotFoundException c)
				{
					System.out.println("Class not found");
					c.printStackTrace();
				}
			} else {
				// save TMC edges if needed.
				try {
					FileOutputStream fos = new FileOutputStream(path.toString());
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(tmcEdges);
					oos.writeObject(osmId2EdgeIds);
					oos.close();
					fos.close();
					System.out.printf("Serialized HashMap data is saved in trafficEdges");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		return gh;
	}

	public RouteSegmentInfo getRouteSegment(double[] latitudes, double[] longitudes, String vehicle,
											EdgeFilter edgeFilter) {
		RouteSegmentInfo result = null;

		GHRequest req = new GHRequest();
		for (int i = 0; i < latitudes.length; i++)
			req.addPoint(new GHPoint(latitudes[i], longitudes[i]));

		req.setVehicle(vehicle);
		req.setAlgorithm("dijkstrabi");
		req.setWeighting("fastest");
		// TODO add limit of maximum visited nodes

		if (edgeFilter != null)
			req.setEdgeFilter(edgeFilter);

		GHResponse resp = new GHResponse();

		List<Path> paths = this.calcPaths(req, resp);

		if (!resp.hasErrors()) {

			List<EdgeIteratorState> fullEdges = new ArrayList<EdgeIteratorState>();
			List<String> edgeNames = new ArrayList<String>();
			PointList fullPoints = PointList.EMPTY;
			long time = 0;
			double distance = 0;
			for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
				Path path = paths.get(pathIndex);
				time += path.getTime();

				for (EdgeIteratorState edge : path.calcEdges()) {
					//	fullEdges.add(edge.getEdge());
					fullEdges.add(edge);
					edgeNames.add(edge.getName());
				}

				PointList tmpPoints = path.calcPoints();

				if (fullPoints.isEmpty())
					fullPoints = new PointList(tmpPoints.size(), tmpPoints.is3D());

				fullPoints.add(tmpPoints);

				distance += path.getDistance();
			}

			if (fullPoints.size() > 1) {
				Coordinate[] coords = new Coordinate[fullPoints.size()];

				for (int i = 0; i < fullPoints.size(); i++) {
					double x = fullPoints.getLon(i);
					double y = fullPoints.getLat(i);
					coords[i] = new Coordinate(x, y);
				}

				//throw new Exception("TODO");
				result = new RouteSegmentInfo(fullEdges, distance, time, new GeometryFactory().createLineString(coords));
			}
		}

		return result;
	}

	public HashMap<Integer, Long> getTmcGraphEdges() {
		return tmcEdges;
	}

	public HashMap<Long, ArrayList<Integer>> getOsmId2EdgeIds() {
		return osmId2EdgeIds;
	}


	/**
	 * Does the preparation and creates the location index
	 */
	@Override
	public void postProcessing() {
		super.postProcessing();

		//TODO: iterate over all available profiles; the hard-coded profile is provided as an example

		int profileType = RoutingProfileType.DRIVING_HGV;

		GraphHopperStorage gs = getGraphHopperStorage();

		/* Initialize edge filter sequence */

		EdgeFilterSequence coreEdgeFilter = new EdgeFilterSequence();

		/* Heavy vehicle filter */

		if (profileType == RoutingProfileType.DRIVING_HGV) {
			coreEdgeFilter.add(new HeavyVehicleCoreEdgeFilter(gs));
		}

		/* Avoid features */

		if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)
				|| profileType == RoutingProfileType.FOOT_WALKING || profileType == RoutingProfileType.FOOT_HIKING
				|| profileType == RoutingProfileType.WHEELCHAIR) {
			coreEdgeFilter.add(new AvoidFeaturesCoreEdgeFilter(gs, profileType));
		}

		/* Avoid borders of some form */

		if (RoutingProfileType.isDriving(profileType) || RoutingProfileType.isCycling(profileType)) {
			coreEdgeFilter.add(new AvoidBordersCoreEdgeFilter(gs));
		}

		/* End filter sequence initialization */

		if(coreFactoryDecorator.isEnabled())
			coreFactoryDecorator.createPreparations(ghStorage, traversalMode, coreEdgeFilter);
		if (!isCorePrepared())
			prepareCore();
	}
	/**
	 * Enables or disables core calculation.
	 */
	public GraphHopper setCoreEnabled(boolean enable) {
		ensureNotLoaded();
		coreFactoryDecorator.setEnabled(enable);
		return this;
	}

	public final boolean isCoreEnabled() {
		return coreFactoryDecorator.isEnabled();
	}

	public void initCoreAlgoFactoryDecorator() {
		if (!coreFactoryDecorator.hasWeightings()) {
			for (FlagEncoder encoder : super.getEncodingManager().fetchEdgeEncoders()) {
				for (String coreWeightingStr : coreFactoryDecorator.getWeightingsAsStrings()) {
					// ghStorage is null at this point
					Weighting weighting = createWeighting(new HintsMap(coreWeightingStr), traversalMode, encoder, null);
					coreFactoryDecorator.addWeighting(weighting);
				}
			}
		}
	}
	public final CoreAlgoFactoryDecorator getCoreFactoryDecorator() {
		return coreFactoryDecorator;
	}

	protected void prepareCore() {
		boolean tmpPrepare = coreFactoryDecorator.isEnabled();
		if (tmpPrepare) {
			ensureWriteAccess();

			ghStorage.freeze();
			coreFactoryDecorator.prepare(ghStorage.getProperties());
			ghStorage.getProperties().put(ORSParameters.Core.PREPARE + "done", true);
		}
	}

	private boolean isCorePrepared() {
		return "true".equals(ghStorage.getProperties().get(ORSParameters.Core.PREPARE + "done"))
				// remove old property in >0.9
				|| "true".equals(ghStorage.getProperties().get("prepare.done"));
	}
}