package rrampage.wasp.data;

import rrampage.wasp.parser.types.ImportDescriptor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Memory - Supports size, grow, load and store
 * This implementation has max limit of 4096 pages i.e 4096 * 64 KiB = 256 MiB
 */
public class Memory {
    private static final int MEM_PAGE_SIZE = 65536;
    public static final int MAX_PAGES = 8192;
    private byte[] memory;
    private final int maxPages;
    private final boolean isShared; // For later work on WASM threads and atomics
    private ByteBuffer buffer;
    public Memory(int pages) {
        this(pages, MAX_PAGES);
    }
    public Memory(int pages, int maxPages) {
        this(pages, maxPages, false);
    }

    public Memory(int pages, int maxPages, boolean isShared) {
        if (pages > MAX_PAGES || maxPages > MAX_PAGES) {
            throw new RuntimeException(String.format("Can not allocate more than %d pages of memory", MAX_PAGES));
        }
        this.maxPages = maxPages;
        this.memory = new byte[pages * MEM_PAGE_SIZE];
        this.isShared = isShared;
        this.buffer = ByteBuffer.wrap(memory).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int getMemorySize() {
        return memory.length/MEM_PAGE_SIZE;
    }

    public int grow(int numPages) {
        int currPages = getMemorySize();
        if (numPages < 0 || currPages + numPages > maxPages) {
            System.out.println(STR."MEM_GROW_LIMIT numPages \{numPages} currPages \{currPages} maxPages \{maxPages}");
            return -1;
        }
        if (numPages == 0) {
            return currPages;
        }
        byte[] newMemory = new byte[(currPages+numPages)*MEM_PAGE_SIZE];
        System.arraycopy(memory, 0, newMemory, 0, memory.length);
        memory = newMemory;
        buffer = ByteBuffer.wrap(memory).order(ByteOrder.LITTLE_ENDIAN);
        return currPages;
    }

    public byte loadByte(int addr) {
        return buffer.get(addr);
    }
    public short loadShort(int addr) {
        return buffer.getShort(addr);
    }
    public int loadInt(int addr) {
        return buffer.getInt(addr);
    }
    public long loadLong(int addr) {
        return buffer.getLong(addr);
    }
    public float loadFloat(int addr) {
        return buffer.getFloat(addr);
    }
    public double loadDouble(int addr) {
        return buffer.getDouble(addr);
    }

    public byte[] load(int addr, int offset) {
        return Arrays.copyOfRange(memory, addr, addr + offset);
    }

    public void store(int addr, long data) {
        buffer.putLong(addr, data);
    }
    public void store(int addr, int data) {
        buffer.putInt(addr, data);
    }
    public void store(int addr, float data) {
        buffer.putFloat(addr, data);
    }
    public void store(int addr, double data) {
        buffer.putDouble(addr, data);
    }
    public void store(int addr, byte data) {
        buffer.put(addr, data);
    }
    public void store(int addr, short data) {
        buffer.putShort(addr, data);
    }

    public void store(int addr, byte[] data) {
        buffer.put(addr, data);
    }

    public void store(int addr, byte[] data, int srcOffset, int numBytes) {
        if (addr + numBytes > memory.length) {
            throw new RuntimeException("Invalid address passed to memory: " + addr);
        }
        if (srcOffset + numBytes > data.length) {
            throw new RuntimeException("Can not copy more than source byte array size");
        }
        buffer.put(addr, data, srcOffset, numBytes);
    }

    public void fill(int addr, byte data, int numBytes) {
        if (addr + numBytes > memory.length) {
            throw new RuntimeException("the destination offset plus size is greater than the length of the target memory");
        }
        Arrays.fill(memory, addr, addr + numBytes, data);
    }

    public String toString() {
        return String.format("Memory: size %d pages, max size: %d pages, shared: %b", getMemorySize(), maxPages, isShared);
    }

    public ByteBuffer buffer() {
        // TODO: Can we make this work even if memory is resized?
        return buffer;
    }

    public boolean matchesDescriptor(ImportDescriptor.MemoryDescriptor m) {
        // TODO : Add check for isShared here as well
        return m.minPages() <= getMemorySize() && m.maxPages() >= maxPages;
    }
}
