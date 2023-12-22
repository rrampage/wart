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
        long startIdx
) {
    /*
        TODO
        Module contains:
        - List of imports
        - List of types
        - List of functions
        - List of exported functions
        - List of tables
        - Data segments
     */

    public String toString() {
        return String.format("""
                Module:
                    Types: %s
                    Functions: %s
                    Exports: %s
                    Data segments: %d
                """, Arrays.toString(types),
                Arrays.toString(functions),
                Arrays.toString(exports),
                dataSegments == null ? 0 : dataSegments.length);
    }
}
