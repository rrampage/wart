package rrampage.wasp.data;

import rrampage.wasp.parser.types.ImportDescriptor;

import java.util.Arrays;

import static rrampage.wasp.data.Variable.NULL_FUNC;
import static rrampage.wasp.data.Variable.REF_NULL;

/**
 * Tables currently can store
 * - function references
 * - External references to types are <a href="https://github.com/WebAssembly/spec/blob/main/proposals/reference-types/Overview.md">proposed to be added</a>
 *
 * For now, only implementing function references
 */
public class Table {
    public static final int MAX_TABLE_SIZE = 8192;
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
        this(size, MAX_TABLE_SIZE, ValueType.RefType.FUNCREF);
    }

    public int size() {
        return this.size;
    }

    public ValueType.RefType type() {
        return type;
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

    public int grow(int delta, int funcIdx) {
        if (delta < 0 || size + delta > max) { return -1;}
        if (delta == 0) { return size;}
        Function[] newData = new Function[size+delta];
        System.arraycopy(data, 0, newData, 0, data.length);
        Function f = (funcIdx == REF_NULL) ? NULL_FUNC : newData[funcIdx];
        Arrays.fill(newData, size, size+delta, f);
        int oldSize = size;
        size = size + delta;
        return oldSize;
    }

    public boolean matchesDescriptor(ImportDescriptor.TableDescriptor d) {
        return this.size >= d.min() && this.max <= d.max() && this.type.equals(d.refType());
    }

    public String toString() {return String.format("Table Type: %s Size: %d Max Size: %d", type.name(), size, max);}
}
