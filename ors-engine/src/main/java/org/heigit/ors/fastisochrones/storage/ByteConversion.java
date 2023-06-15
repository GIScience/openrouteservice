package org.heigit.ors.fastisochrones.storage;

import java.nio.ByteBuffer;

public class ByteConversion {

    public static final String ERROR_MSG_BYTECOUNT = "Byte counts do not match, expected %d but is %d";

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
            throw new IllegalArgumentException(String.format(ERROR_MSG_BYTECOUNT, Double.BYTES, bytes.length));
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static long byteArrayToLong(byte[] bytes) {
        if (bytes.length != Long.BYTES)
            throw new IllegalArgumentException(String.format(ERROR_MSG_BYTECOUNT, Long.BYTES, bytes.length));
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static int byteArrayToInteger(byte[] bytes) {
        if (bytes.length != Integer.BYTES)
            throw new IllegalArgumentException(String.format(ERROR_MSG_BYTECOUNT, Integer.BYTES, bytes.length));
        return ByteBuffer.wrap(bytes).getInt();
    }
}
