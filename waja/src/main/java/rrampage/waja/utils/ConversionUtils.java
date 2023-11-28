package rrampage.waja.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ConversionUtils {
    public static ByteBuffer bytesToBuffer(byte[] data) {
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }
    public static byte[] longToBytes(long val) {
        return bytesToBuffer(new byte[Long.BYTES]).putLong(val).array();
    }

    public static long bytesToLong(byte[] data) {
        return bytesToBuffer(data).getLong();
    }

    public static byte[] intToBytes(int val) {
        return bytesToBuffer(new byte[Integer.BYTES]).putInt(val).array();
    }

    public static int bytesToInt(byte[] data) {
        return bytesToBuffer(data).getInt();
    }

    public static byte[] shortToBytes(short val) {
        return bytesToBuffer(new byte[Short.BYTES]).putShort(val).array();
    }

    public static short bytesToShort(byte[] data) {
        return bytesToBuffer(data).getShort();
    }

    public static byte[] floatToBytes(float val) {
        return bytesToBuffer(new byte[Float.BYTES]).putFloat(val).array();
    }

    public static float bytesToFloat(byte[] data) {
        return bytesToBuffer(data).getFloat();
    }

    public static byte[] doubleToBytes(double val) {
        return bytesToBuffer(new byte[Double.BYTES]).putDouble(val).array();
    }

    public static double bytesToDouble(byte[] data) {
        return bytesToBuffer(data).getDouble();
    }

    public static int wrapBoolean(boolean val) {
        return (val) ? 1 : 0;
    }

    public static long intToLong(int val) {
        return val; // no-op
    }

    public static int longToInt(long val) {
        return (int) val; // cast down
    }

    public static long floatToLong(float val) {
        return Float.floatToIntBits(val);
    }

    public static float longToFloat(long val) {
        return Float.intBitsToFloat((int) val);
    }

    public static long doubleToLong(double val) {
        return Double.doubleToLongBits(val);
    }

    public static double longToDouble(long val) {
        return Double.longBitsToDouble(val);
    }
}
