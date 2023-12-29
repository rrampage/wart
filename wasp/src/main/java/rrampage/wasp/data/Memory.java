package rrampage.wasp.data;

import java.util.Arrays;

/**
 * Memory - Supports size, grow, load and store
 * This implementation has max limit of 256 pages i.e 1024 * 64 KiB = 64 MiB
 */
public class Memory {
    private static final int MEM_PAGE_SIZE = 65536;
    public static final int MAX_PAGES = 4096;
    private byte[] memory;
    private final int maxPages;
    private final boolean isShared; // For later work on WASM threads and atomics
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
    }

    public int getMemorySize() {
        return memory.length/MEM_PAGE_SIZE;
    }

    public int grow(int numPages) {
        int currPages = getMemorySize();
        if (numPages < 0 || currPages + numPages > maxPages) {
            return -1;
        }
        if (numPages == 0) {
            return currPages;
        }
        byte[] newMemory = new byte[(currPages+numPages)*MEM_PAGE_SIZE];
        System.arraycopy(memory, 0, newMemory, 0, memory.length);
        memory = newMemory;
        return -1;
    }

    public byte[] load(int addr, int offset) {
        return Arrays.copyOfRange(memory, addr, addr + offset);
    }

    public void store(int addr, byte[] data) {
        if (addr >= memory.length) {
            throw new RuntimeException("Invalid address passed to memory: " + addr);
        }
        System.arraycopy(data, 0, memory, addr, data.length);
    }

    public void store(int addr, byte[] data, int srcOffset, int numBytes) {
        if (addr >= memory.length || addr + numBytes >= memory.length) {
            throw new RuntimeException("Invalid address passed to memory: " + addr);
        }
        if (srcOffset + numBytes >= data.length) {
            throw new RuntimeException("Can not copy more than source byte array size");
        }
        System.arraycopy(data, srcOffset, memory, addr, numBytes);
    }

    public String toString() {
        return String.format("Memory: size %d pages, max size: %d pages, shared: %b", getMemorySize(), maxPages, isShared);
    }
}
