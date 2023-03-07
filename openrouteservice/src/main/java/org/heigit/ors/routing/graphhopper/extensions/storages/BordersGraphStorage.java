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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.*;

/**
 * Graph storage class for the Border Restriction routing
 */
public class BordersGraphStorage implements GraphExtension {
	public enum Property { TYPE, START, END}
	/* pointer for no entry */
	protected static final int NO_ENTRY = -1;
	private static final int EF_BORDER = 0;		// byte location of border type
	private static final int EF_START = 2;			// byte location of the start country id
	private static final int EF_END = 4;			// byte location of the end country id

	// border types
	public static final short NO_BORDER = 0;
	public static final short OPEN_BORDER = 2;
	public static final short CONTROLLED_BORDER = 1;

	private DataAccess orsEdges;
	private int edgeEntryBytes;
	private int edgesCount; // number of edges with custom values

	public BordersGraphStorage() {

		int edgeEntryIndex = 0;
		edgeEntryBytes = edgeEntryIndex + 6;	// item uses 3 short values which are 2 bytes length each
		edgesCount = 0;
	}

	/**
	 * Set values to the edge based on the border type and countries<br/><br/>
	 *
	 * This method takes the internal ID of the edge and adds the information obtained from the Borders CSV file to it
	 * so that the values can be taken into account when generating a route.
	 *  @param edgeId        Internal ID of the graph edge
	 * @param borderType    Level of border crossing (0 - No border, 1 - controlled border, 2 - open border=
	 * @param start            ID of the country that the edge starts in
	 * @param end            ID of the country that the edge ends in
	 */
	public void setEdgeValue(int edgeId, short borderType, short start, short end) {
		edgesCount++;
		ensureEdgesIndex(edgeId);

		// add entry
		long edgePointer = (long) edgeId * edgeEntryBytes;

		orsEdges.setShort(edgePointer + EF_BORDER, borderType);
		orsEdges.setShort(edgePointer + EF_START, start);
		orsEdges.setShort(edgePointer + EF_END, end);
	}

	private void ensureEdgesIndex(int edgeId) { orsEdges.ensureCapacity(((long) edgeId + 1) * edgeEntryBytes); }

	/**
	 * Get the specified custom value of the edge that was assigned to it in the setValueEdge method<br/><br/>
	 * <p>
	 * The method takes an identifier to the edge and then gets the requested value for the edge from the storage
	 *
	 * @param edgeId Internal ID of the edge to get values for
	 * @param prop   The property of the edge to get (TYPE - border type (0,1,2), START - the ID of the country
	 *               the edge starts in, END - the ID of the country the edge ends in.
	 * @return The value of the requested property
	 */
	public short getEdgeValue(int edgeId, Property prop) {
		long edgePointer = (long) edgeId * edgeEntryBytes;
		short border = orsEdges.getShort(edgePointer + EF_BORDER);
		short start = orsEdges.getShort(edgePointer + EF_START);
		short end = orsEdges.getShort(edgePointer + EF_END);

		switch (prop) {
			case TYPE:
				return border;
			case START:
				return start;
			case END:
				return end;
			default:
				return 0;
		}

	}

	/**
	 * initializes the extended storage by giving the base graph
	 *
	 * @param graph
	 * @param dir
	 */
	@Override
	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.orsEdges = dir.find("ext_borders");
	}

	/**
	 * initializes the extended storage to be empty - required for testing purposes as the ext_storage aren't created
	 * at the time tests are run
	 */
	public void init() {
		if(edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");
		Directory d = new RAMDirectory();
		this.orsEdges = d.find("");
	}

	/**
	 * @return true if successfully loaded from persistent storage.
	 */
	@Override
	public boolean loadExisting() {
		if (!orsEdges.loadExisting())
			throw new IllegalStateException("Unable to load storage 'ext_borders'. corrupt file or directory?");

		edgeEntryBytes = orsEdges.getHeader(0);
		edgesCount = orsEdges.getHeader(4);
		return true;
	}

	/**
	 * Creates the underlying storage. First operation if it cannot be loaded.
	 *
	 * @param initBytes
	 */
	@Override
	public BordersGraphStorage create(long initBytes) {
		orsEdges.create(initBytes * edgeEntryBytes);
		return this;
	}

	/**
	 * This method makes sure that the underlying data is written to the storage. Keep in mind that
	 * a disc normally has an IO cache so that flush() is (less) probably not save against power
	 * loses.
	 */
	@Override
	public void flush() {
		orsEdges.setHeader(0, edgeEntryBytes);
		orsEdges.setHeader(4, edgesCount);
		orsEdges.flush();
	}

	/**
	 * This method makes sure that the underlying used resources are released. WARNING: it does NOT
	 * flush on close!
	 */
	@Override
	public void close() { orsEdges.close(); }

	@Override
	public boolean isClosed() {
		return false;
	}

	/**
	 * @return the allocated storage size in bytes
	 */
	@Override
	public long getCapacity() {
		return orsEdges.getCapacity();
	}
}
