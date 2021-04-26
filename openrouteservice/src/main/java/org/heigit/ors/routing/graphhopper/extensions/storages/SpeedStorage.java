package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Storable;

public class SpeedStorage implements Storable<SpeedStorage> {
    private final DataAccess speedData;
    private int edgeCount;
    private final int byteCount = 2;

    public SpeedStorage(int edgeCount, Directory dir) {
        speedData = dir.find("isochronenodes");
        this.edgeCount = edgeCount;
    }

    @Override
    public boolean loadExisting() {
        return false;
    }

    @Override
    public SpeedStorage create(long l) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public long getCapacity() {
        return 0;
    }
}
