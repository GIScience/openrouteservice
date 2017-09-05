package heigit.ors.routing.graphhopper.extensions.storages;

/**
 * This class is used to create the shortest-path-tree from linked entities.
 * <p>
 *
 */
public class MultiTreeSPEntry implements Cloneable, Comparable<MultiTreeSPEntry> {
	public int[] edge;
	public int adjNode;
	public double[] weights;
	public boolean[] update;
	public MultiTreeSPEntry[] parent;
	public boolean visited = false;

	public MultiTreeSPEntry(int adjNode, int numTrees) {
		this.edge = new int[numTrees];
		this.adjNode = adjNode;
		this.weights = new double[numTrees];
		this.update = new boolean[numTrees];
		this.parent = new MultiTreeSPEntry[numTrees];
	}

	/**
	 * This method returns the weight to the origin e.g. to the start for the
	 * forward SPT and to the destination for the backward SPT. Where the
	 * variable 'weight' is used to let heap select smallest *full* weight (from
	 * start to destination).
	 */
	public double[] getWeightOfVisitedPath() {
		return weights;
	}

	@Override
	public MultiTreeSPEntry clone() {
		MultiTreeSPEntry res = new MultiTreeSPEntry(adjNode, edge.length);

		for (int i = 0; i < edge.length; i++) {
			res.edge[i] = edge[i];
			res.weights[i] = weights[i];
			res.parent[i] = parent[i];
		}

		return res;
	}

	public MultiTreeSPEntry cloneFull() {
		throw new UnsupportedOperationException("cloneFull not supported");
	}

	@Override
	public int compareTo(MultiTreeSPEntry o) {
		double s1 = 0;
		double s2 = 0;
		for (int i = 0; i < weights.length; i++) {
			s1 += weights[i];
			s2 += o.weights[i];
		}
		if (s1 < s2)
			return -1;

		// assumption no NaN and no -0
		return s1 > s2 ? 1 : 0;
	}

	@Override
	public String toString() {
		return adjNode + " (" + edge + ") weight: " + weights;
	}
}
