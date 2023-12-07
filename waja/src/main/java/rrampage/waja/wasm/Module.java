package rrampage.waja.wasm;

import rrampage.waja.wasm.data.Function;
import rrampage.waja.wasm.data.FunctionType;

import java.util.Arrays;
import java.util.HashMap;

public record Module(FunctionType[] types, Function[] functions, HashMap<String, Function> exports) {
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
