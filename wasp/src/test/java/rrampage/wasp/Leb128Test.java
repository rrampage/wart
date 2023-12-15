package rrampage.wasp;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.utils.Leb128;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

public class Leb128Test {

    @TestFactory
    public Stream<DynamicTest> testLeb128Unsigned() {
        record TestCase(String name, byte[] inp, long val) {
            public void check() {
                assertEquals(val, Leb128.readUnsigned(ByteBuffer.wrap(inp)), name);
            }
        }

        var testCases = new TestCase[] {
                new TestCase("zero", new byte[]{0x0}, 0x0),
                new TestCase("smallByte1", new byte[]{0x70}, 0x70),
                new TestCase("smallByte2", new byte[]{0x40}, 64),
                new TestCase("smallByteHighest", new byte[]{0x7F}, 127),
                new TestCase("twoByteLowest", new byte[]{(byte) 0x80, 0x01}, 128),
                new TestCase("twoByte1", new byte[]{(byte) 0x81, 0x01}, 129),
                new TestCase("fourByte1", new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xfd, 0x07}, 2141192192),
                new TestCase("i32max", new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07}, Integer.MAX_VALUE),
                new TestCase("long(-1)", new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01}, -1L),
        };
        return DynamicTest.stream(Stream.of(testCases), TestCase::name, TestCase::check);
    }

    @TestFactory
    public Stream<DynamicTest> testLeb128Signed() {
        record TestCase(String name, byte[] inp, long val) {
            public void check() {
                assertEquals(val, Leb128.readSigned(ByteBuffer.wrap(inp)), name);
            }
        }

        var testCases = new TestCase[] {
                new TestCase("zero", new byte[]{0x0}, 0x0),
                new TestCase("smallByte1", new byte[]{0x70}, -16),
                new TestCase("smallByte2", new byte[]{0x40}, -64),
                new TestCase("smallByteHighest", new byte[]{0x7F}, -1),
                new TestCase("twoByteLowest", new byte[]{(byte) 0x80, 0x01}, 128),
                new TestCase("twoByte1", new byte[]{(byte) 0x81, 0x01}, 129),
                new TestCase("threeByte1", new byte[]{(byte) 0xc0, (byte) 0xbb, 0x78}, -123456),
                new TestCase("fourByte1", new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xfd, 0x07}, 2141192192),
                new TestCase("i32max", new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07}, Integer.MAX_VALUE),
                new TestCase("i32min", new byte[]{(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x78}, Integer.MIN_VALUE),
                new TestCase("long(-1)", new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01}, -1L),
        };
        return DynamicTest.stream(Stream.of(testCases), TestCase::name, TestCase::check);
    }
}
