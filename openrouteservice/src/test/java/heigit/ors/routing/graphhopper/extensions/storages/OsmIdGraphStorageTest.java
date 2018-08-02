package heigit.ors.routing.graphhopper.extensions.storages;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OsmIdGraphStorageTest {
    private final OsmIdGraphStorage _storage;

    public OsmIdGraphStorageTest() {
        _storage = new OsmIdGraphStorage();
        _storage.init();
        _storage.create(1);
    }

    @Test
    public void TestItemCreation() {
        _storage.setEdgeValue(1, 1234L);

        assertEquals(1234, _storage.getEdgeValue(1));
    }
}
