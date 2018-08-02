package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.*;
import heigit.ors.routing.graphhopper.extensions.util.EncodeUtils;

public class OsmIdGraphStorage implements GraphExtension {
    /* pointer for no entry */
    protected final int NO_ENTRY = -1;
    protected final int EF_OSMID;

    protected DataAccess orsEdges;
    protected int edgeEntryIndex = 0;
    protected int edgeEntryBytes;
    protected int edgesCount; // number of edges with custom values

    private byte[] byteValues;

    public OsmIdGraphStorage() {
        EF_OSMID = 0;
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

    public void setSegmentSize(int bytes) {
        orsEdges.setSegmentSize(bytes);
    }

    public GraphExtension create(long initBytes) {
        orsEdges.create(initBytes * edgeEntryBytes);
        return this;
    }

    public void flush() {
        orsEdges.setHeader(0, edgeEntryBytes);
        orsEdges.setHeader(1 * 4, edgesCount);
        orsEdges.flush();
    }

    public void close() {
        orsEdges.close();
    }

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
        orsEdges.setBytes(edgePointer + EF_OSMID, byteValues, 4);
    }

    /**
     * Get the OSM id of the edge specified
     * @param edgeId    The internal graph id of the edge that the OSM way ID is required for
     * @return          The OSM ID that was stored for the edge (normally the OSM ID of the way the edge was created from)
     */
    public long getEdgeValue(int edgeId) {
        byte[] buffer = new byte[4];
        long edgePointer = (long) edgeId * edgeEntryBytes;
        orsEdges.getBytes(edgePointer + EF_OSMID, buffer, 4);

        return EncodeUtils.byteArrayToLong(buffer);
    }

    public boolean isRequireNodeField() {
        return false;
    }

    public boolean isRequireEdgeField() {
        // we require the additional field in the graph to point to the first
        // entry in the node table
        return true;
    }

    public int getDefaultNodeFieldValue() {
        return -1; //throw new UnsupportedOperationException("Not supported by this storage");
    }

    public int getDefaultEdgeFieldValue() {
        return -1;
    }

    public GraphExtension copyTo(GraphExtension clonedStorage) {
        if (!(clonedStorage instanceof OsmIdGraphStorage)) {
            throw new IllegalStateException("the extended storage to clone must be the same");
        }

        OsmIdGraphStorage clonedTC = (OsmIdGraphStorage) clonedStorage;

        orsEdges.copyTo(clonedTC.orsEdges);
        clonedTC.edgesCount = edgesCount;

        return clonedStorage;
    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }
}
