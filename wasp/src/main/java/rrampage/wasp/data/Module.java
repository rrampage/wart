package rrampage.wasp.data;

import java.util.Arrays;
import java.util.HashMap;

public record Module(FunctionType[] types, Function[] functions, Table[] tables, HashMap<String, Function> exports, HashMap<String, Integer> importMap) {
    /*
        TODO
        Module contains:
        - List of imports
        - List of types
        - List of functions
        - List of exported functions
        - List of tables
        - Data
     */

    public String toString() {
        return String.format("""
                Module:
                    Types: %s
                    Functions: %s
                    Exports: %s
                """, Arrays.toString(types), Arrays.toString(functions), exports);
    }
}
