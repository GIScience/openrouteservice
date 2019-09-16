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
package org.heigit.ors.routing.algorithms;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.profiles.EnumEncodedValue;
import com.graphhopper.routing.profiles.IntEncodedValue;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.*;
import org.apache.log4j.Logger;

public class SubGraph {
	private final Logger logger = Logger.getLogger(getClass());

	private GHIntObjectHashMap<EdgeIteratorLink> node2EdgesMap;
	private Graph baseGraph;

	class EdgeIteratorLink  {
		private EdgeIteratorState state;
		private EdgeIteratorLink next;

		public EdgeIteratorState getState() {
			return state;
		}

		public void setState(EdgeIteratorState state) {
			this.state = state;
		}

		public EdgeIteratorLink getNext() {
			return next;
		}

		public void setNext(EdgeIteratorLink next) {
			this.next = next;
		}

		public EdgeIteratorLink(EdgeIteratorState iterState)
		{
			state = iterState;
		}
	}

	class SubGraphEdgeExplorer implements EdgeExplorer {
		private SubGraph graph;

		public SubGraphEdgeExplorer(SubGraph graph) {
			this.graph = graph;
		}

		@Override
		public EdgeIterator setBaseNode(int baseNode) {
			return graph.setBaseNode(baseNode);
		}
	}

	class EdgeIteratorLinkIterator implements EdgeIterator, CHEdgeIteratorState {
		private EdgeIteratorState currState;
		private EdgeIteratorLink link;
		private boolean firstRun = true;

		public EdgeIteratorLinkIterator(EdgeIteratorLink link) {
			this.link = link;
			currState = link.state;
		}

		@Override
		public int getEdge() {
			return currState.getEdge();
		}

		@Override
		public int getOrigEdgeFirst() {
			return currState.getOrigEdgeFirst();
		}

		@Override
		public int getOrigEdgeLast() {
			return currState.getOrigEdgeLast();
		}

		@Override
		public int getBaseNode() {
			return currState.getBaseNode();
		}

		@Override
		public int getAdjNode() {
			return currState.getAdjNode();
		}

		@Override
		public PointList fetchWayGeometry(int mode) {
			return null;
		}

		@Override
		public EdgeIteratorState setWayGeometry(PointList list) {
			return null;
		}

		@Override
		public double getDistance() {
			return currState.getDistance();
		}

		@Override
		public EdgeIteratorState setDistance(double dist) {
			return null;
		}

		@Override
		public IntsRef getFlags() {
			return currState.getFlags();
		}

		@Override
		public EdgeIteratorState setFlags(IntsRef edgeFlags) {
			return currState.setFlags(edgeFlags);
		}

		@Override
		public int getAdditionalField() {
			return 0;
		}

		@Override
		public EdgeIteratorState setAdditionalField(int value) {
			return null;
		}

		@Override
		public boolean get(BooleanEncodedValue property) {
			return currState.get(property);
		}

		@Override
		public EdgeIteratorState set(BooleanEncodedValue property, boolean value) {
			return currState.set(property, value);
		}

		@Override
		public boolean getReverse(BooleanEncodedValue property) {
			return currState.getReverse(property);
		}

		@Override
		public EdgeIteratorState setReverse(BooleanEncodedValue property, boolean value) {
			return currState.setReverse(property, value);
		}

		@Override
		public int get(IntEncodedValue property) {
			return currState.get(property);
		}

		@Override
		public EdgeIteratorState set(IntEncodedValue property, int value) {
			return currState.set(property, value);
		}

		@Override
		public int getReverse(IntEncodedValue property) {
			return currState.getReverse(property);
		}

		@Override
		public EdgeIteratorState setReverse(IntEncodedValue property, int value) {
			return currState.setReverse(property, value);
		}

		@Override
		public double get(DecimalEncodedValue property) {
			return currState.get(property);
		}

		@Override
		public EdgeIteratorState set(DecimalEncodedValue property, double value) {
			return currState.set(property, value);
		}

		@Override
		public double getReverse(DecimalEncodedValue property) {
			return currState.getReverse(property);
		}

		@Override
		public EdgeIteratorState setReverse(DecimalEncodedValue property, double value) {
			return currState.setReverse(property, value);
		}

		@Override
		public <T extends Enum> T get(EnumEncodedValue<T> property) {
			return currState.get(property);
		}

		@Override
		public <T extends Enum> EdgeIteratorState set(EnumEncodedValue<T> property, T value) {
			return currState.set(property, value);
		}

		@Override
		public <T extends Enum> T getReverse(EnumEncodedValue<T> property) {
			return currState.getReverse(property);
		}

		@Override
		public <T extends Enum> EdgeIteratorState setReverse(EnumEncodedValue<T> property, T value) {
			return currState.setReverse(property, value);
		}

		@Override
		public String getName() {
			return currState.getName();
		}

		@Override
		public EdgeIteratorState setName(String name) {
			return null;
		}

		@Override
		public EdgeIteratorState detach(boolean reverse) {
			return currState.detach(reverse);
		}

		@Override
		public EdgeIteratorState copyPropertiesFrom(EdgeIteratorState e) {
			return null;
		}


		@Override
		public boolean next() {
			if (firstRun)
			{
				firstRun = false;
				return true;
			}

			link = link.next;

			if (link == null)
			{
				currState = null;

				return false;
			}

			currState = link.state;

			return true;
		}

		@Override
		public int getSkippedEdge1() {
			return 0;
		}

		@Override
		public int getSkippedEdge2() {
			return 0;
		}

		@Override
		public CHEdgeIteratorState setSkippedEdges(int edge1, int edge2) {
			return this;
		}

		@Override
		public CHEdgeIteratorState setFirstAndLastOrigEdges(int firstOrigEdge, int lastOrigEdge) {
			throw new IllegalStateException("Unsupported operation");
		}

		@Override
		public boolean isShortcut() {
			if (currState instanceof CHEdgeIteratorState)
				return (((CHEdgeIteratorState) currState).isShortcut());
			else 
				return false;
		}

		@Override
		public int getMergeStatus(int flags) {
			return 0;
		}


		@Override
		public double getWeight() {
			return (((CHEdgeIteratorState) currState).getWeight());
		}

		@Override
		public CHEdgeIteratorState setWeight(double weight) {
			return null;
		}

		@Override
		public void setFlagsAndWeight(int flags, double weight) {
			// do nothing
		}
	}

	public SubGraph(Graph graph) {
		baseGraph = graph;
		node2EdgesMap = new GHIntObjectHashMap<>(Math.min(Math.max(200, graph.getNodes() / 10), 2000));
	}

	/**
	 * Returns true/false depending on whether node is already in the graph or not.
	 */
	public boolean addEdge(int adjNode, EdgeIteratorState iter, boolean reverse) {
		if (iter == null) {
			node2EdgesMap.put(adjNode, null);
			return true;
		}

		EdgeIteratorState iterState = null;
		if (reverse) {
			iterState =  baseGraph.getEdgeIteratorState(iter.getEdge(), adjNode);
			adjNode = iter.getAdjNode();
		} else {
			iterState =  baseGraph.getEdgeIteratorState(iter.getEdge(), iter.getAdjNode());
			adjNode = iter.getBaseNode();
		}

		EdgeIteratorLink link = node2EdgesMap.get(adjNode);
		if (link == null) {
			link = new EdgeIteratorLink(iterState);
			node2EdgesMap.put(adjNode, link);
			return true;
		} else {
			while (link.next != null)
				link = link.next;
			link.next = new EdgeIteratorLink(iterState);
			return false;
		}
	}

	public boolean containsNode(int adjNode)
	{
		return node2EdgesMap.containsKey(adjNode);
	}

	public EdgeIterator setBaseNode(int baseNode) {
		EdgeIteratorLink link = node2EdgesMap.get(baseNode);
		return link == null ? null: new EdgeIteratorLinkIterator(link);
	}

	public EdgeExplorer createExplorer()
	{
		return new SubGraphEdgeExplorer(this);
	}

	public void print() {
		int edgesCount = 0;

		EdgeExplorer explorer = createExplorer();

		for (IntObjectCursor<?> node : node2EdgesMap) {
			EdgeIterator iter = explorer.setBaseNode(node.key);

			if (iter != null)
			{
				while (iter.next())
				{
					edgesCount++;
				}
			}
		}

		logger.info("SubGraph: nodes - " + node2EdgesMap.size() + "; edges - " + edgesCount);
	}
}
