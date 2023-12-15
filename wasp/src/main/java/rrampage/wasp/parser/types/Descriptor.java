package rrampage.wasp.parser.types;

public sealed interface Descriptor {
    record FunctionDescriptor(int idx) implements Descriptor {public String toString() {return "Function - Type " + idx();}}
    record MemoryDescriptor(int minPages, int maxPages) implements Descriptor {public String toString() {return "Memory - Min %d Max %d".formatted(minPages(), maxPages());}}
    record TableDescriptor(ValueType refType, int min, int max) implements Descriptor {public String toString() {return "Table - Type %s Min %d Max %d".formatted(refType().name(), min(), max());}}
    record GlobalDescriptor(ValueType valueType, boolean mutable) implements Descriptor {public String toString() {return "Global - Type %s Mutable %b".formatted(valueType().name(), mutable());}}
}
