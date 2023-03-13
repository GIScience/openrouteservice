package org.heigit.ors.routing.graphhopper.extensions.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncodeUtilsTest {
    @Test
    void longToByteArrayTest() {
        long value = 1234L;
        byte[] byteValue = EncodeUtils.longToByteArray(value);

        assertEquals(8, byteValue.length);
        assertEquals(-46, byteValue[7]);
        assertEquals(4, byteValue[6]);
    }

    @Test
    void byteArrayToLongTest() {
        // 01001001 10010110 00000010 11010010
        byte[] byteArr = {0,0,0,0,73,-106,2,-46};
        long value = EncodeUtils.byteArrayToLong(byteArr);

        assertEquals(1234567890, value);

        // 11101000 01101010 00000100 10011111
        byte[] byteArr2 = {-24,106,4,-97};
        long value2 = EncodeUtils.byteArrayToLong(byteArr2);

        assertEquals(3899262111L, value2);

        // 01111100 00000000 00000000 00001100 11010011 01001100 11111000 10100000 00000001
        byte[] byteArr3 = {124,0,0,12,-45,76,-8,-96,1};
        long value3 = EncodeUtils.byteArrayToLong(byteArr3);

        assertEquals(14101668995073L, value3);
    }
}
