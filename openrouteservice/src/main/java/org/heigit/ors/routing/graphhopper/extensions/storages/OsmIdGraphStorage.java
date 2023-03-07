package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.*;
import org.heigit.ors.routing.graphhopper.extensions.util.EncodeUtils;

public class OsmIdGraphStorage implements GraphExtension {
    /* pointer for no entry */
    protected final int efOsmid;

    protected DataAccess orsEdges;
    protected int edgeEntryIndex = 0;
    protected int edgeEntryBytes;
    protected int edgesCount; // number of edges with custom values

    private final byte[] byteValues;

    public OsmIdGraphStorage() {
        efOsmid = 0;
        edgeEntryBytes = edgeEntryIndex + 4;
        edgesCount = 0;
        byteValues = new byte[4];
    }

    public void init(Graph graph, Directory dir) {
        if (edgesCount > 0)
            throw new AssertionError("The ORS storage must be initialized only once.");

        this.orsEdges = dir.find("ext_osmids");
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

    public OsmIdGraphStorage create(long initBytes) {
        orsEdges.create(initBytes * edgeEntryBytes);
        return this;
    }

    public void flush() {
        orsEdges.setHeader(0, edgeEntryBytes);
        orsEdges.setHeader(4, edgesCount);
        orsEdges.flush();
    }

    public void close() {
        orsEdges.close();
    }

    @Override
    public long getCapacity() {
        return orsEdges.getCapacity();
    }

    public int entries() {
        return edgesCount;
    }

    public boolean loadExisting() {
        if (!orsEdges.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_osmids'. corrupt file or directory? " );

        edgeEntryBytes = orsEdges.getHeader(0);
        edgesCount = orsEdges.getHeader(4);
        return true;
    }

    void ensureEdgesIndex(int edgeIndex) {
        orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
    }

    /**
     * Set the osm id of an edge to the spcified value.
     * @param edgeId    The internal id of the edge in the graph
     * @param osmId     The osm idto be assigned ot the edge
     */
    public void setEdgeValue(int edgeId, long osmId) {
        edgesCount++;
        ensureEdgesIndex(edgeId);

        // add entry
        long edgePointer = (long) edgeId * edgeEntryBytes;
        byte[] tempBytes = EncodeUtils.longToByteArray(osmId);
        byteValues[0] = tempBytes[4];
        byteValues[1] = tempBytes[5];
        byteValues[2] = tempBytes[6];
        byteValues[3] = tempBytes[7];
        orsEdges.setBytes(edgePointer + efOsmid, byteValues, 4);
    }

    /**
     * Get the OSM id of the edge specified
     * @param edgeId    The internal graph id of the edge that the OSM way ID is required for
     * @return          The OSM ID that was stored for the edge (normally the OSM ID of the way the edge was created from)
     */
    public long getEdgeValue(int edgeId) {
        byte[] buffer = new byte[4];
        long edgePointer = (long) edgeId * edgeEntryBytes;
        orsEdges.getBytes(edgePointer + efOsmid, buffer, 4);

        return EncodeUtils.byteArrayToLong(buffer);
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
