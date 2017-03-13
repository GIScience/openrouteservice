/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov

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

import heigit.ors.mapmatching.RouteSegmentInfo;
import heigit.ors.routing.RoutingProfile;
import heigit.ors.routing.graphhopper.extensions.storages.builders.GraphStorageBuilder;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

public class ORSGraphHopper extends GraphHopper {

	private Envelope _bbox;
	private List<GraphStorageBuilder> _storageBuilders;
	private HashMap<Integer, Long> tmcEdges;
	
	// A route profile for referencing which is used to extract names of adjacent streets and other objects.
	private RoutingProfile refRouteProfile;

	public ORSGraphHopper(Envelope bbox, List<GraphStorageBuilder> storageBuilders, boolean useTmc, RoutingProfile refProfile) {
		this._bbox = bbox;
		this._storageBuilders = storageBuilders;
		this.refRouteProfile= refProfile;
		this.setSimplifyResponse(false);
		
		if (useTmc)
			tmcEdges = new HashMap<Integer, Long>();
	}
	
	protected DataReader createReader(GraphHopperStorage tmpGraph) {
		return initOSMReader(new ORSOSMReader(tmpGraph, _bbox, _storageBuilders, tmcEdges, refRouteProfile));
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

		if (tmcEdges != null) {
			java.nio.file.Path path = Paths.get(gh.getGraphHopperLocation(), "edges_ors_traffic");

			if (tmcEdges.size() == 0) {
				// try to load TMC edges from file.
				try {
					File file = path.toFile();

					if (file.exists())
					{
						FileInputStream fis = new FileInputStream(path.toString());
						ObjectInputStream ois = new ObjectInputStream(fis);
						tmcEdges = (HashMap<Integer, Long>)ois.readObject();
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

		if (edgeFilter != null)
			req.setEdgeFilter(edgeFilter);

		GHResponse resp = new GHResponse();

		List<Path> paths = this.getPaths(req, resp);

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
}
