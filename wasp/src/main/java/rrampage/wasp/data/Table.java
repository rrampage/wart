package rrampage.wasp.data;

import rrampage.wasp.parser.types.ImportDescriptor;

/**
 * Tables currently can store
 * - function references
 * - External references to types are <a href="https://github.com/WebAssembly/spec/blob/main/proposals/reference-types/Overview.md">proposed to be added</a>
 *
 * For now, only implementing function references
 */
public class Table {
    private int size;
    private final int max;
    private Function[] data;
    private final ValueType.RefType type;
    public Table(int initialSize, int maxSize, ValueType.RefType type) {
        /*if (!type.equals(ValueType.RefType.FUNCREF) || initialSize > maxSize) {
            throw new RuntimeException("Invalid table init");
        }*/
        this.size = initialSize;
        this.max = maxSize;
        this.data = new Function[this.size];
        this.type = type;
    }

    public Table(int size) {
        this(size, size, ValueType.RefType.FUNCREF);
    }

    public int size() {
        return this.size;
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

    public boolean matchesDescriptor(ImportDescriptor.TableDescriptor d) {
        return this.size == d.min() && this.max == d.max() && this.type.equals(d.refType());
    }

    public String toString() {return String.format("Table Type: %s Size: %d Max Size: %d", type.name(), size, max);}
}
