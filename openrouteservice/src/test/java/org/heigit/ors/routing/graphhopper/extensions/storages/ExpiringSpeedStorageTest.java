package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.storage.RAMDirectory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExpiringSpeedStorageTest {
    @Test
    public void testCreation(){
        ExpiringSpeedStorage storage = new ExpiringSpeedStorage(new CarFlagEncoder());
        storage.init(null, new RAMDirectory(""));
        storage.create(4);
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(0, false));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(1, false));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(2, false));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(3, false));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(0, true));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(1, true));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(2, true));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(3, true));
    }

    @Test
    public void testSetGetSpeed() {
        ExpiringSpeedStorage storage = new ExpiringSpeedStorage(new CarFlagEncoder());
        storage.init(null, new RAMDirectory(""));
        storage.create(4);
        storage.setSpeed(0, false, 20);
        assertEquals(20, storage.getSpeed(0, false));
        storage.setSpeed(0, false, 25);
        assertEquals(25, storage.getSpeed(0, false));
        assertEquals(Byte.MIN_VALUE, storage.getSpeed(0, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooHighSpeed(){
        ExpiringSpeedStorage storage = new ExpiringSpeedStorage(new CarFlagEncoder());
        storage.init(null, new RAMDirectory(""));
        storage.create(4);
        storage.setSpeed(0, false, 128);
    }
}
