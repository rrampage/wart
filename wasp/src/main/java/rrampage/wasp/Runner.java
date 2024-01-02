package rrampage.wasp;

import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.ValueType;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.ImportUtils;

import java.nio.file.Paths;
import java.util.Map;

public class Runner {
    public static void main(String[] args) throws Exception {
        // TODO Load and run wasm file. How to handle imports?
        // (import "host" "print" (func $hprint (param i32) (result i32)))
        String fileName = "./examples/add_two.wasm";
        String path = Paths.get(fileName).toAbsolutePath().normalize().toString();
        var parser = WasmParser.fromFile(path);
        var module = parser.parseModule();
        var machine = module.instantiate(Map.of("host", Map.of("print", ImportUtils.generateLoggerHandle(new FunctionType(new ValueType.NumType[]{ValueType.NumType.I32}, new ValueType.NumType[]{ValueType.NumType.I32})))));
    }
}
