package org.heigit.ors.common;

import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncoderNameEnumTest {

    @Test
    void testGetName() {
        assertEquals("driving-car", EncoderNameEnum.DRIVING_CAR.getEncoderName());
    }

    @Test
    void testGetValue() {
        assertEquals(1, EncoderNameEnum.DRIVING_CAR.getValue());
    }

    @Test
    void testIsDriving() {
        assertTrue(EncoderNameEnum.isDriving(1));
        assertFalse(EncoderNameEnum.isDriving(10));
    }

    @Test
    void testIsHeavyVehicle() {
        assertTrue(EncoderNameEnum.isHeavyVehicle(2));
        assertFalse(EncoderNameEnum.isHeavyVehicle(1));
    }

    @Test
    void testIsWalking() {
        assertTrue(EncoderNameEnum.isWalking(20));
        assertFalse(EncoderNameEnum.isWalking(1));
    }

    @Test
    void testIsPedestrian() {
        assertTrue(EncoderNameEnum.isPedestrian(20));
        assertTrue(EncoderNameEnum.isPedestrian(30));
        assertFalse(EncoderNameEnum.isPedestrian(1));
    }

    @Test
    void testIsWheelchair() {
        assertTrue(EncoderNameEnum.isWheelchair(30));
        assertFalse(EncoderNameEnum.isWheelchair(1));
    }

    @Test
    void testIsCycling() {
        assertTrue(EncoderNameEnum.isCycling(10));
        assertFalse(EncoderNameEnum.isCycling(1));
    }

    @Test
    void testSupportMessages() {
        assertTrue(EncoderNameEnum.supportMessages(1));
        assertFalse(EncoderNameEnum.supportMessages(10));
    }

    @Test
    void testGetNameByValue() {
        assertEquals("driving-car", EncoderNameEnum.getName(1));
        assertEquals("default", EncoderNameEnum.getName(99));
    }

    @Test
    void testGetFromString() {
        assertEquals(1, EncoderNameEnum.getFromString("driving-car"));
        assertEquals(0, EncoderNameEnum.getFromString("unknown-type"));
    }

    @Test
    void testGetEncoderName() {
        assertEquals(FlagEncoderNames.CAR_ORS, EncoderNameEnum.getEncoderName(1));
        assertEquals(FlagEncoderNames.GH_FOOT, EncoderNameEnum.getEncoderName(31));
        assertEquals(FlagEncoderNames.GH_FOOT, EncoderNameEnum.getEncoderName(46));
        assertEquals(FlagEncoderNames.UNKNOWN, EncoderNameEnum.getEncoderName(99));
    }

    @Test
    void testGetFromEncoderName() {
        assertEquals(1, EncoderNameEnum.getFromEncoderName(FlagEncoderNames.CAR_ORS));
        assertEquals(20, EncoderNameEnum.getFromEncoderName(FlagEncoderNames.GH_FOOT));
        assertEquals(20, EncoderNameEnum.getFromEncoderName(FlagEncoderNames.PEDESTRIAN_ORS));
        assertEquals(30, EncoderNameEnum.getFromEncoderName(FlagEncoderNames.WHEELCHAIR));
        assertEquals(0, EncoderNameEnum.getFromEncoderName("unknown-encoder"));
    }
}