package rrampage.wasp.utils;

public class MathUtils {
    public static final int FLOAT_CANONICAL_NAN_BITS = 255<<23 |1<<22;
    public static final int FLOAT_CANONICAL_NEG_NAN_BITS = -FLOAT_CANONICAL_NAN_BITS;
    public static final float FLOAT_CANONICAL_NAN = Float.intBitsToFloat(FLOAT_CANONICAL_NAN_BITS);
    public static final float FLOAT_CANONICAL_NAN_NEG = Float.intBitsToFloat(FLOAT_CANONICAL_NEG_NAN_BITS);
    public static final long DOUBLE_CANONICAL_NAN_BITS = 2047L<<52 |1L<<51;
    public static final long DOUBLE_CANONICAL_NEG_NAN_BITS = -DOUBLE_CANONICAL_NAN_BITS;
    public static final double DOUBLE_CANONICAL_NAN = Double.longBitsToDouble(DOUBLE_CANONICAL_NAN_BITS);

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
