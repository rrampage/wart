package rrampage.wasp.data;

import java.util.Arrays;

/**
 * Memory - Supports size, grow, load and store
 * This implementation has max limit of 256 pages i.e 1024 * 64 KiB = 64 MiB
 */
public class Memory {
    private static final int MEM_PAGE_SIZE = 65536;
    private static final int MAX_PAGES = 1024;
    private byte[] memory;
    public Memory(int pages) {
        if (pages > MAX_PAGES) {
            throw new RuntimeException(String.format("Can not allocate more than %d pages of memory", MAX_PAGES));
        }
        this.memory = new byte[pages * MEM_PAGE_SIZE];
    }

    public int getMemorySize() {
        return memory.length/MEM_PAGE_SIZE;
    }

    public int grow(int numPages) {
        int currPages = getMemorySize();
        if (numPages < 0 || currPages + numPages > MAX_PAGES) {
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

}
