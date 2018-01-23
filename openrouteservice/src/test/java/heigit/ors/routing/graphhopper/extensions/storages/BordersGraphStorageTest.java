package heigit.ors.routing.graphhopper.extensions.storages;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BordersGraphStorageTest {

    private final BordersGraphStorage _storage;

    public BordersGraphStorageTest() {
        _storage = new BordersGraphStorage();
        _storage.init();
        _storage.create(1);
    }

    @Test
    public void TestItemCreation() {
        _storage.setEdgeValue(1, (short)1, (short)2, (short)3);

        assertEquals(_storage.getEdgeValue(1, BordersGraphStorage.Property.TYPE), 1);
        assertEquals(_storage.getEdgeValue(1, BordersGraphStorage.Property.START), 2);
        assertEquals(_storage.getEdgeValue(1, BordersGraphStorage.Property.END), 3);
    }
}
