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

package org.freeopenls.routeservice.traffic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class TrafficLocationGraph {
	private class LocationEdge extends DefaultEdge
	{
		private Integer source;
		private Integer target;
		
		public LocationEdge(Integer source, Integer target)
		{
			this.source = source;
			this.target = target;
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = -3996637280511152711L;

		public Integer getSource()
		{
			return source;
		}

		public Integer getTarget()
		{
			return target;
		}
	}

	private DirectedGraph<Integer, LocationEdge> graph;

	private TrafficLocationGraph()
	{
		EdgeFactory<Integer, LocationEdge> ef = new EdgeFactory<Integer, TrafficLocationGraph.LocationEdge>() {
			
			@Override
			public LocationEdge createEdge(Integer sourceVertex, Integer targetVertex) {
				// TODO Auto-generated method stub
				return new LocationEdge(sourceVertex, targetVertex);
			}
		};
		graph = new DefaultDirectedGraph<Integer, LocationEdge>(ef);
	}
	
	public boolean containsCode(int code)
	{
		return graph.containsVertex(code);
	}

	public List<Integer> getShortestPath(int startVertex, int endVertex)
	{
		List<Integer> result = new ArrayList<Integer>();

		DijkstraShortestPath<Integer, LocationEdge> dijkstra = new DijkstraShortestPath<Integer, LocationEdge>(graph, startVertex, endVertex);
		
		for(LocationEdge edge : dijkstra.getPathEdgeList())
		{
			if (!result.contains(edge.getSource()))
				result.add(edge.getSource());
			if (!result.contains(edge.getTarget()))
				result.add(edge.getTarget());
		}

		return result;
	}

	public static TrafficLocationGraph createFromFile(File file) 
	{
		TrafficLocationGraph tlg = new TrafficLocationGraph();

		try {
			BufferedReader brPoffsets = new BufferedReader(new FileReader(file));
			String line = null;
			DirectedGraph<Integer, LocationEdge> graph = tlg.graph;

			while ((line = brPoffsets.readLine()) != null) {
				if ((!line.startsWith("#")) && (!line.equals(""))) {

					String[] tmp = line.split(";");
					
					if (tmp[2].equals(""))
						continue;
					int loc0 = Integer.parseInt(tmp[2]);

					if (!graph.containsVertex(loc0))
						graph.addVertex(loc0);

					for(int i = 3; i< tmp.length; i++)
					{
						if (tmp[i].equals(""))
							continue;
						
						int loc1 = Integer.parseInt(tmp[i]);

						if (!graph.containsVertex(loc1))
							graph.addVertex(loc1);

						graph.addEdge(loc0, loc1);
					}
				}
			}

			brPoffsets.close();
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//tlg.getShortestPath(36716, 52807);

		return tlg;
	}
}
