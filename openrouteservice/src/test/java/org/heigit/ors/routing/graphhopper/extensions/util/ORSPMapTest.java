package org.heigit.ors.routing.graphhopper.extensions.util;

import com.graphhopper.util.PMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ORSPMapTest {
    @Test
    void longToByteArrayTest() {
        ORSPMap map = new ORSPMap();
        int[] obj = {1, 2, 3};
        map.putObj("some_key", obj);
        PMap parent = map;
        parent.put("another_key", 1);
        map = (ORSPMap)parent;

        assertEquals(obj, map.getObj("some_key"));
        assertEquals("1", map.get("another_key", "default"));
    }

}
