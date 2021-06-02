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

//    @Test(expected = IllegalArgumentException.class)
//    public void testEdgeIdOutOfBounds() {
//        ExpiringSpeedStorage storage = new ExpiringSpeedStorage(new CarFlagEncoder());
//        storage.init(null, new RAMDirectory(""));
//        storage.create(4);
//        storage.getSpeed(4, true);
//    }

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


    //TODO Fix powermock so this can be used
//    @Test
//    public void testExpiredSpeed(){
//        ExpiringSpeedStorage storage = new ExpiringSpeedStorage(new CarFlagEncoder());
//        storage.init(null, new RAMDirectory(""));
//        storage.create(4);
//        storage.setDefaultExpirationTime(1);
//        storage.setSpeed(0, false, 100);
//        assertEquals(100, storage.getSpeed(0, false));
//        // Set to some late
//        Instant future = Instant.now().plus(2, ChronoUnit.MINUTES);
//        mockStatic(Instant.class);
//        when(Instant.now()).thenReturn(future);
//
//        assertEquals(Byte.MIN_VALUE, storage.getSpeed(0, false));
//    }
}
