package rrampage.wasp.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MathUtils {
    public static final int FLOAT_CANONICAL_NAN_BITS = 255<<23 |1<<22;
    public static final int FLOAT_CANONICAL_NEG_NAN_BITS = -FLOAT_CANONICAL_NAN_BITS;
    public static final float FLOAT_CANONICAL_NAN = Float.intBitsToFloat(FLOAT_CANONICAL_NAN_BITS);
    public static final float FLOAT_CANONICAL_NAN_NEG = Float.intBitsToFloat(FLOAT_CANONICAL_NEG_NAN_BITS);
    public static final long DOUBLE_CANONICAL_NAN_BITS = 2047L<<52 |1L<<51;
    public static final long DOUBLE_CANONICAL_NEG_NAN_BITS = -DOUBLE_CANONICAL_NAN_BITS;
    public static final double DOUBLE_CANONICAL_NAN = Double.longBitsToDouble(DOUBLE_CANONICAL_NAN_BITS);
    public static final BigInteger MAX_UNSIGNED_LONG = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
    public static final double MAX_UNSIGNED_LONG_DOUBLE_VAL = MAX_UNSIGNED_LONG.doubleValue();
    public static final float MAX_UNSIGNED_LONG_FLOAT_VAL = MAX_UNSIGNED_LONG.floatValue();
    public static long MAX_UNSIGNED_INT = Integer.MAX_VALUE * 2L + 1L;

    public static float nearest(float a) {
        if (isCanonicalNaN(a) || Float.isNaN(a) || Float.isInfinite(a) || a == 0.0f) {return a;}
        if (Float.MAX_VALUE == a || -Float.MAX_VALUE == a) {return a;}
        if (a > 0.0f && a <= 0.5f) {return 0.0f;}
        if (a < 0.0f && a >= -0.5f) {return -0.0f;}
        return -Math.round(-a);
    }

    public static double nearest(double a) {
        if (isCanonicalNaN(a) || Double.isNaN(a) || Double.isInfinite(a) || a == 0.0) {return a;}
        if (Double.MAX_VALUE == a || -Double.MAX_VALUE == a) {return a;}
        if (a > 0.0 && a <= 0.5) {return 0.0;}
        if (a < 0.0 && a >= -0.5) {return -0.0;}
        return -Math.round(-a);
    }

    public static boolean isCanonicalNaN(float a) {
        return Float.floatToRawIntBits(a) == FLOAT_CANONICAL_NAN_BITS || Float.floatToRawIntBits(a) == FLOAT_CANONICAL_NEG_NAN_BITS;
    }

    public static boolean isCanonicalNaN(double a) {
        return Double.doubleToRawLongBits(a) == DOUBLE_CANONICAL_NAN_BITS || Double.doubleToRawLongBits(a) == DOUBLE_CANONICAL_NEG_NAN_BITS;
    }

    public static long truncateDoubleToUnsignedLong(double d) {
        System.out.println(d);
        if (Double.isNaN(d) || d <= 0.0) {
            return 0L;
        }
        if (d >= MAX_UNSIGNED_LONG_DOUBLE_VAL) {
            return -1L;
        }
        if (d < Long.MAX_VALUE) {
            return (long) d;
        }
        return (long)(Math.floor(d) - MAX_UNSIGNED_LONG_DOUBLE_VAL);
    }

    public static long truncateFloatToUnsignedLong(float f) {
        if (Float.isNaN(f) || f <= 0.0f) {
            return 0L;
        }
        if (f >= MAX_UNSIGNED_LONG_FLOAT_VAL) {
            return -1L;
        }
        if (f < Long.MAX_VALUE) {
            return (long) f;
        }
        return (long)(Math.floor(f) - MAX_UNSIGNED_LONG_FLOAT_VAL);
    }

    public static int truncateDoubleToUnsignedInt(double d) {
        return (Double.isNaN(d) || d <= 0.0) ? 0 :
                ((d >= MAX_UNSIGNED_INT) ? -1 :
                    d < Integer.MAX_VALUE ? (int) d : (int) ((long)d));
    }

    public static int truncateFloatToUnsignedInt(float f) {
        if (Float.isNaN(f) || f <= 0.0f) {
            return 0;
        }
        if (f >= MAX_UNSIGNED_INT) {
            return -1;
        }
        if (f < Integer.MAX_VALUE) {
            return (int) f;
        }
        return (int) ((long)f);
    }

    public static void main(String[] args) {
        int x = 1;
        for (int i = 1; i < 8; i++) {
            int n = x << (23+i);
            int m = n | 0x90000000;
            System.out.printf("Int: %x %d %s Float: %f\n", n, n, Integer.toUnsignedString(n), Float.intBitsToFloat(n));
            System.out.printf("Int: %x %d %s Float: %f\n", m, m, Integer.toUnsignedString(m), Float.intBitsToFloat(m));
        }
    }
}
