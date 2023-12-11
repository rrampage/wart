package rrampage.wasp.data;

/**
 * Tables currently can store
 * - function references
 * - External references to types are <a href="https://github.com/WebAssembly/spec/blob/main/proposals/reference-types/Overview.md">proposed to be added</a>
 *
 * For now, only implementing function references
 */
public class Table {
    private static final String FUNCREF = "funcref";
    private final int index;
    private int size;
    private final int max;
    private Function[] data;
    public Table(int index, int initialSize, int maxSize, String type) {
        if (!type.equals(FUNCREF) || initialSize > maxSize || index < 0) {
            throw new RuntimeException("Invalid table init");
        }
        this.index = index;
        this.size = initialSize;
        this.max = maxSize;
        this.data = new Function[this.size];
    }

    public Table(int index, int size) {
        this(index, size, size, FUNCREF);
    }

    public Function get(int i) {
        return this.data[i];
    }

    public void set(int i, Function f) {
        if (i >= data.length && i < max) {
            Function[] newData = new Function[max];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
            size = max;
        }
        this.data[i] = f;
    }

    public int index() {
        return index;
    }
}
