package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.RAMDirectory;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.junit.Test;

import static org.junit.Assert.*;

public class WheelchairAttributesGraphStorageTest {
    private WheelchairAttributesGraphStorage storage;


    public WheelchairAttributesGraphStorageTest() {
    }

    @Test
    public void WheelchairEdgeCreationTest() {
        storage = new WheelchairAttributesGraphStorage();
        storage.init(null, new RAMDirectory(""));
        storage.create(1);

        WheelchairAttributes attrs = new WheelchairAttributes();
        attrs.setAttribute(WheelchairAttributes.Attribute.WIDTH, 180);
        attrs.setAttribute(WheelchairAttributes.Attribute.KERB, 3);
        attrs.setReliableAttribute(WheelchairAttributes.Attribute.SURFACE, 1);
        attrs.setReliableAttribute(WheelchairAttributes.Attribute.SMOOTHNESS, 1);
        attrs.setReliableAttribute(WheelchairAttributes.Attribute.INCLINE, 0);


        storage.setEdgeValues(1, attrs);

        WheelchairAttributes attrsRet = new WheelchairAttributes();

        storage.getEdgeValues(1, attrsRet, new byte[WheelchairAttributesGraphStorage.BYTE_COUNT]);

        assertEquals(180, attrsRet.getWidth(), 0.0);
        assertEquals(3, attrsRet.getSlopedKerbHeight(), 0.0);
        assertTrue(attrsRet.isSurfaceReliable());
        assertTrue(attrsRet.isSmoothnessReliable());
        assertFalse(attrsRet.isTrackTypeReliable());
        assertTrue(attrsRet.isInclineReliable());
        assertFalse(attrsRet.isWidthReliable());

    }

}
