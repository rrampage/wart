package rrampage.wasp.data;

import rrampage.wasp.parser.types.ExportMetadata;
import rrampage.wasp.parser.types.ImportMetadata;

import java.util.Arrays;

public record Module(
        int version,
        FunctionType[] types,
        Function[] functions,
        Table[] tables,
        ExportMetadata[] exports,
        ImportMetadata[] imports,
        Memory[] memories,
        DataSegment[] dataSegments,
        ElementSegment[] elementSegments,
        Variable[] globals,
        long startIdx
) {
    public String toString() {
        return String.format("""
                Module:
                    Types: %s
                    Functions: %s
                    Exports: %s
                    Globals: %s
                    Data segments: %d
                """, Arrays.toString(types),
                Arrays.toString(functions),
                Arrays.toString(exports),
                Arrays.toString(globals),
                dataSegments == null ? 0 : dataSegments.length);
    }
}
