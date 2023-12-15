package rrampage.wasp.utils;

import java.nio.ByteBuffer;

public class Leb128 {
    private static final int MAX_4 = Math.ceilDiv(32, 7);
    private static final int MAX_8 = Math.ceilDiv(64, 7);

    public static long readUnsigned(ByteBuffer in) {
        long sum = 0L;
        int s = 0, c;
        do {
            c = in.get() & 0xff;
            sum |= ((long) (c&0x7f) << s);
            s += 7;
        } while ((c & 0x80) != 0);
        return sum;
    }
    public static long readSigned(ByteBuffer in) {
        long sum = 0L;
        int s = 0, c;
        long signBits = -1;
        do {
            c = in.get() & 0xff;
            sum |= ((long) (c & 0x7f)) << (s*7);
            signBits <<= 7;
            s+=1;
        } while (((c & 0x80) != 0) && s < MAX_8);
        // Sign extend if appropriate
        if (((signBits >> 1) & sum) != 0 ) {
            sum |= signBits;
        }
        return sum;
    }
}
