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
        attrs.setAttribute(WheelchairAttributes.Attribute.WIDTH, 180, false);
        attrs.setAttribute(WheelchairAttributes.Attribute.KERB, 3, false);
        attrs.setAttribute(WheelchairAttributes.Attribute.INCLINE, 0, false);
        attrs.setAttribute(WheelchairAttributes.Attribute.SURFACE, 1, true);
        attrs.setAttribute(WheelchairAttributes.Attribute.SMOOTHNESS, 1, true);
        attrs.setSuitable(true);


        storage.setEdgeValues(1, attrs);

        WheelchairAttributes attrsRet = new WheelchairAttributes();

        storage.getEdgeValues(1, attrsRet, new byte[WheelchairAttributesGraphStorage.BYTE_COUNT]);

        assertEquals(180, attrsRet.getWidth(), 0.0);
        assertEquals(3, attrsRet.getSlopedKerbHeight(), 0.0);
        assertTrue(attrsRet.isSurfaceQualityKnown());
        assertTrue(attrsRet.isSuitable());

    }

}
