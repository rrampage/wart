package rrampage.wasp.parser.types;

public record ImportMetadata(String module, String name, ImportDescriptor descriptor) {
    public sealed interface ImportDescriptor {}
    public record FunctionDescriptor(int idx) implements ImportDescriptor {public String toString() {return "Function - Type " + idx();}}
    public record MemoryDescriptor(int minPages, int maxPages) implements ImportDescriptor {public String toString() {return "Memory - Min %d Max %d".formatted(minPages(), maxPages());}}
    public record TableDescriptor(ValueType refType, int min, int max) implements ImportDescriptor {public String toString() {return "Table - Type %s Min %d Max %d".formatted(refType().name(), min(), max());}}
    public record GlobalDescriptor(ValueType valueType, boolean mutable) implements ImportDescriptor {public String toString() {return "Global - Type %s Mutable %b".formatted(valueType().name(), mutable());}}
}
