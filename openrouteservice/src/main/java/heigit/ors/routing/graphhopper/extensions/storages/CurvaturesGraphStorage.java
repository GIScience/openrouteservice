package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

public class CurvaturesGraphStorage implements GraphExtension {
    /* pointer for no entry */
    private final int EF_CURVATURE;

    private DataAccess orsEdges;
    private int edgeEntryBytes;
    private int edgesCount; // number of edges with custom values

    private byte[] byteValues;

    public CurvaturesGraphStorage() {
        EF_CURVATURE = 0;

        int edgeEntryIndex = 0;
        edgeEntryBytes = edgeEntryIndex + 1;
        edgesCount = 0;
        byteValues = new byte[1];
    }

    public void setEdgeValue(int edgeId, double sinousity) {
        edgesCount++;
        ensureEdgesIndex(edgeId);

        // add entry
        long edgePointer = (long) edgeId * edgeEntryBytes;
        // multiply double by byte range, result is still double
        sinousity *= 128;
        // cast to int
        int intSinousity = (int) sinousity;
        // cast to byte
        byteValues[0] = (byte) intSinousity;
        orsEdges.setBytes(edgePointer + EF_CURVATURE, byteValues, 1);
    }

    private void ensureEdgesIndex(int edgeId) {
        orsEdges.ensureCapacity(((long) edgeId + 1) * edgeEntryBytes);
    }

    public int getEdgeValue(int edgeId, byte[] buffer) {
        // TODO this needs further checking when implementing the Weighting classes/functions
        long edgePointer = (long) edgeId * edgeEntryBytes;
        orsEdges.getBytes(edgePointer + EF_CURVATURE, buffer, 1);

        return buffer[0];
    }

    /**
     * @return true, if and only if, if an additional field at the graphs node storage is required
     */
    @Override
    public boolean isRequireNodeField() {
        // TODO I don't know what's this method for, just refer to that in the HillIndex class
        return true;
    }

    /**
     * @return true, if and only if, if an additional field at the graphs edge storage is required
     */
    @Override
    public boolean isRequireEdgeField() {
        // TODO I don't know what's this method for, just refer to that in the HillIndex class
        return true;
    }

    /**
     * @return the default field value which will be set for default when creating nodes
     */
    @Override
    public int getDefaultNodeFieldValue() {
        // TODO I don't know what's this method for, just refer to that in the HillIndex class
        return -1;
    }

    /**
     * @return the default field value which will be set for default when creating edges
     */
    @Override
    public int getDefaultEdgeFieldValue() {
        // TODO I don't know what's this method for, just refer to that in the HillIndex class
        return -1;
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

        this.orsEdges = dir.find("ext_greenindex");
    }

    /**
     * sets the segment size in all additional data storages
     *
     * @param bytes
     */
    @Override
    public void setSegmentSize(int bytes) {
        orsEdges.setSegmentSize(bytes);
    }

    /**
     * creates a copy of this extended storage
     *
     * @param clonedStorage
     */
    @Override
    public GraphExtension copyTo(GraphExtension clonedStorage) {
        if (!(clonedStorage instanceof CurvaturesGraphStorage)) {
            throw new IllegalStateException("the extended storage to clone must be the same");
        }

        CurvaturesGraphStorage clonedTC = (CurvaturesGraphStorage) clonedStorage;

        orsEdges.copyTo(clonedTC.orsEdges);
        clonedTC.edgesCount = edgesCount;

        return clonedStorage;
    }

    /**
     * @return true if successfully loaded from persistent storage.
     */
    @Override
    public boolean loadExisting() {
        if (!orsEdges.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_curvatures'. corrupt file or directory?");

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
    public GraphExtension create(long initBytes) {
        orsEdges.create((long) initBytes * edgeEntryBytes);
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
        orsEdges.setHeader(1 * 4, edgesCount);
        orsEdges.flush();
    }

    /**
     * This method makes sure that the underlying used resources are released. WARNING: it does NOT
     * flush on close!
     */
    @Override
    public void close() {
        orsEdges.close();
    }

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
