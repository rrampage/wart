package rrampage.wasp.parser.types;

public sealed interface ExportDescriptor {
    record FunctionDescriptor(int idx) implements ExportDescriptor {public String toString() {return "Function - Type " + idx;}}
    record MemoryDescriptor(int idx) implements ExportDescriptor {public String toString() {return "Memory - Idx %d".formatted(idx);}}
    record TableDescriptor(int idx) implements ExportDescriptor {public String toString() {return "Table - Idx %d".formatted(idx);}}
    record GlobalDescriptor(int idx) implements ExportDescriptor {public String toString() {return "Global - Idx %d".formatted(idx);}}
}
