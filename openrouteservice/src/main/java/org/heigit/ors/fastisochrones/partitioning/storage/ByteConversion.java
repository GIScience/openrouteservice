package org.heigit.ors.fastisochrones.partitioning.storage;

import java.nio.ByteBuffer;

public class ByteConversion {
    private ByteConversion() {
    }

    public static byte[] doubleToByteArray(double value) {
        byte[] bytes = new byte[Double.BYTES];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static byte[] longToByteArray(long value) {
        byte[] bytes = new byte[Long.BYTES];
        ByteBuffer.wrap(bytes).putLong(value);
        return bytes;
    }

    public static byte[] intToByteArray(int value) {
        byte[] bytes = new byte[Integer.BYTES];
        ByteBuffer.wrap(bytes).putInt(value);
        return bytes;
    }

    public static double byteArrayToDouble(byte[] bytes) {
        if (bytes.length != Double.BYTES)
            throw new IllegalArgumentException("Byte counts do not match, expected " + Double.BYTES + " but is " + bytes.length);
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static long byteArrayToLong(byte[] bytes) {
        if (bytes.length != Long.BYTES)
            throw new IllegalArgumentException("Byte counts do not match, expected " + Long.BYTES + " but is " + bytes.length);
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static int byteArrayToInteger(byte[] bytes) {
        if (bytes.length != Integer.BYTES)
            throw new IllegalArgumentException("Byte counts do not match, expected " + Integer.BYTES + " but is " + bytes.length);
        return ByteBuffer.wrap(bytes).getInt();
    }
}
