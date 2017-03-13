package heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

/**
 * Created by lliu on 13/03/2017.
 */
public class GreenIndexGraphStorage implements GraphExtension {
    /**
     * @return true, if and only if, if an additional field at the graphs node storage is required
     */
    @Override
    public boolean isRequireNodeField() {
        return false;
    }

    /**
     * @return true, if and only if, if an additional field at the graphs edge storage is required
     */
    @Override
    public boolean isRequireEdgeField() {
        return false;
    }

    /**
     * @return the default field value which will be set for default when creating nodes
     */
    @Override
    public int getDefaultNodeFieldValue() {
        return 0;
    }

    /**
     * @return the default field value which will be set for default when creating edges
     */
    @Override
    public int getDefaultEdgeFieldValue() {
        return 0;
    }

    /**
     * initializes the extended storage by giving the base graph
     *
     * @param graph
     * @param dir
     */
    @Override
    public void init(Graph graph, Directory dir) {

    }

    /**
     * sets the segment size in all additional data storages
     *
     * @param bytes
     */
    @Override
    public void setSegmentSize(int bytes) {

    }

    /**
     * creates a copy of this extended storage
     *
     * @param extStorage
     */
    @Override
    public GraphExtension copyTo(GraphExtension extStorage) {
        return null;
    }

    /**
     * @return true if successfully loaded from persistent storage.
     */
    @Override
    public boolean loadExisting() {
        return false;
    }

    /**
     * Creates the underlying storage. First operation if it cannot be loaded.
     *
     * @param byteCount
     */
    @Override
    public GraphExtension create(long byteCount) {
        return null;
    }

    /**
     * This method makes sure that the underlying data is written to the storage. Keep in mind that
     * a disc normally has an IO cache so that flush() is (less) probably not save against power
     * loses.
     */
    @Override
    public void flush() {

    }

    /**
     * This method makes sure that the underlying used resources are released. WARNING: it does NOT
     * flush on close!
     */
    @Override
    public void close() {

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
        return 0;
    }
}
