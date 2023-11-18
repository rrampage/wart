package rrampage.waja.utils;

import java.nio.ByteBuffer;

public class ConversionUtils {
    public static byte[] longToBytes(long val) {
        return ByteBuffer.wrap(new byte[Long.BYTES]).putLong(val).array();
    }

    public static long bytesToLong(byte[] data) {
        return ByteBuffer.wrap(data).getLong();
    }

    public static int wrapBoolean(boolean val) {
        return (val) ? 1 : 0;
    }
}
